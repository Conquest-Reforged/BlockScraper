package me.dags.scraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import me.dags.scraper.asset.AssetManager;
import me.dags.scraper.asset.AssetPath;
import me.dags.scraper.asset.blockstate.BlockState;
import me.dags.scraper.asset.model.Model;
import me.dags.scraper.asset.model.ModelType;
import me.dags.scraper.dynmap.ModelRegistrar;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author dags <dags@dags.me>
 */
@Mod(modid = BlockScraper.MOD_ID, name = "BlockScraper", version = "0.4.1", dependencies = "required-after:dynmap", serverSideOnly = true, acceptableRemoteVersions = "*")
public class BlockScraper {

    public static final String MOD_ID = "blockscraper";
    public static final Logger logger = LogManager.getLogger("BlockScraper");

    private static boolean debug = false;

    private File configDir = new File("");
    private boolean scrape = true;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(), "blockscraper");
        configDir.mkdirs();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        File configFile = new File(configDir, "config.json");

        Collection<String> mods = Config.findBlockProvidingMods();
        logger.info("Detected {} block providing mods", mods.size());

        logger.info("Reading config...");
        Config config = Config.read(configFile);

        logger.info("Detecting setup changes...");
        if (config.doScrape() || !config.hasAll(mods)) {
            logger.info("Detected mod changes or scrape force-enabled, running on server start");
            Config.write(configFile, mods);
            scrape = true;
        } else {
            scrape = false;
            logger.info("No changes detected, skipping BlockScrape");
        }
    }

    @Mod.EventHandler
    public void serverStart(FMLServerAboutToStartEvent event) {
        File mcDir = Loader.instance().getConfigDir().getParentFile();

        if (scrape) {
            scrape(mcDir);
        } else {
            restore(mcDir);
        }
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        File mcDir = Loader.instance().getConfigDir().getParentFile();

        if (scrape) {
            backup(mcDir);
        }
    }

    private void restore(File mcDir) {
        File backups = new File(configDir, "backups");
        if (!backups.exists()) {
            logger.info("No backup render data detected");
            return;
        }

        File[] files = backups.listFiles();
        if (files == null) {
            logger.info("No backup render data detected");
            return;
        }

        logger.info("Restoring mod render data from backup");
        Path modsupport = mcDir.toPath().resolve("dynmap").resolve("renderdata").resolve("modsupport");
        for (File from : files) {
            Path to = modsupport.resolve(from.getName());
            copy(from.toPath(), to);
        }
    }

    private void scrape(File mcDir) {
        // Tell ModelRegistrar where to extract textures to. Must happen before registering blocks
        ModelRegistrar.getInstance().setMCDir(mcDir);

        // Iterate over ModContainers and collect assets into one virtual (in-memory) pack of assets
        AssetManager.getInstance().findAssets();

        // Loop through block registry and attempt to generate dynmodels for them
        BlockScraper.registerBlocks();

        // Tell dynmap we're done registering models/textures
        ModelRegistrar.getInstance().publish();

        // Clear assets & cached resources
        AssetManager.getInstance().clear();

        // Clear cached ModTextureDefinition and TextureFile references
        ModelRegistrar.getInstance().clear();
    }

    private void backup(File mcDir) {
        File modsupport = mcDir.toPath().resolve("dynmap").resolve("renderdata").resolve("modsupport").toFile();
        if (!modsupport.exists()) {
            logger.info("No dynmap render data detected");
            return;
        }

        File[] files = modsupport.listFiles();
        if (files == null) {
            logger.info("No dynmap render data detected");
            return;
        }

        logger.info("Backing up mod render data");
        Path backups = configDir.toPath().resolve("backups");
        for (File from : files) {
            Path to = backups.resolve(from.getName());
            copy(from.toPath(), to);
        }
    }

    private static void copy(Path from, Path to) {
        try {
            Files.createDirectories(to.getParent());
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerBlocks() {
        for (Block block : Block.REGISTRY) {
            // Don't register models/textures for vanilla blocks. Dynmap handles these
            if (!block.getRegistryName().getResourceDomain().equals("minecraft")) {
                registerBlock(block);
            }
        }
    }

    private static void registerBlock(Block block) {
        try {
            AssetPath statePath = AssetPath.of(block.getRegistryName(), "blockstates");
            BlockState blockState = BlockState.forPath(statePath);

            if (blockState != null) {
                if (blockState.getModelType() != ModelType.CUSTOM) {
                    // blockstate json for this block specifies one of Dynmap's built-in models to use
                    registerVariant(block, block.getDefaultState(), 0, blockState);
                } else if (blockState.hasVariants()) {
                    // attempt to build and register custom models from models/block jsons for each variant/meta
                    Set<Integer> visited = new HashSet<>();
                    for (IBlockState variant : block.getBlockState().getValidStates()) {
                        int meta = block.getMetaFromState(variant);
                        if (visited.add(meta)) {
                            registerVariant(block, variant, meta, blockState);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Error registering block: {}", block.getRegistryName());
            if (debug) {
                t.printStackTrace();
            }
        }
    }

    private static void registerVariant(Block block, IBlockState variant, int meta, BlockState blockState) {
        try {
            ResourceLocation registryName = block.getRegistryName();
            String domain = registryName.getResourceDomain();
            String name = registryName.getResourcePath();
            String query = StateMapper.getBlockstateStateQuery(variant);

            Model model = blockState.getModel(query);
            if (model != null) {
                ModelRegistrar.getInstance().register(domain, name, meta, blockState.getModelType(), model);
            }
        } catch (Throwable t) {
            logger.error("Error registering model {} {}", variant, t.getLocalizedMessage());
            if (debug) {
                t.printStackTrace();
            }
        }
    }
}
