package me.dags.scraper;

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
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
@Mod(modid = BlockScraper.MOD_ID, name = "BlockScraper", version = "1.0", dependencies = "required-after:Dynmap", serverSideOnly = true, acceptableRemoteVersions = "*")
public class BlockScraper {

    public static final String MOD_ID = "blockscraper";
    public static final Logger logger = LogManager.getLogger("BlockScraper");
    private static boolean debug = false;

    @Mod.EventHandler
    public void serverStart(FMLServerAboutToStartEvent event) {
        File mcDir = Loader.instance().getConfigDir().getParentFile();

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
            logger.error("Error registering variant: {}", variant);
            if (debug) {
                t.printStackTrace();
            }
        }
    }
}
