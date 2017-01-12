package me.dags.scraper;

import me.dags.scraper.asset.AssetContainer;
import me.dags.scraper.asset.AssetManager;
import me.dags.scraper.asset.blockstate.BlockState;
import me.dags.scraper.asset.model.Model;
import me.dags.scraper.asset.model.ModelType;
import me.dags.scraper.asset.util.ResourcePath;
import me.dags.scraper.dynmap.ModelRegistrar;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
@Mod(modid = BlockScraper.MOD_ID, version = "1.0")
public class BlockScraper {

    public static final String MOD_ID = "blockscraper";
    private boolean debug = true;

    @Mod.EventHandler
    public void serverStart(FMLServerAboutToStartEvent event) {
        // Tell ModelRegistrar where to extract textures to. Must happen before registering blocks
        ModelRegistrar.getInstance().setMCDir(Loader.instance().getConfigDir().getParentFile());

        // Scan for ModContainers and add to AssetManager
        findAssets();

        // Loop through block registry and attempt to generate dynmodels for them
        registerBlocks();

        // Clear references to ModContainers & cached resources
        AssetManager.getInstance().clear();

        // Tell dynmap we're done registering models/textures
        ModelRegistrar.getInstance().publish();

        // Clear cached ModTextureDefinition and TextureFile references
        ModelRegistrar.getInstance().clear();
    }

    private void findAssets() {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            AssetContainer container = new AssetContainer(mod.getModId(), mod.getSource());
            if (mod.getModId().equals(MOD_ID)) {
                // Add self as the root AssetContainer (other mods override me)
                AssetManager.getInstance().setDefaultContainer(container);
            } else {
                // Add other to containers list
                AssetManager.getInstance().addContainer(container);
            }
        }
    }

    private void registerBlocks() {
        for (Block block : Block.REGISTRY) {
            // Don't register models/textures for vanilla blocks. Dynmap handles these
            if (!block.getRegistryName().getResourceDomain().equals("minecraft")) {
                registerBlock(block);
            }
        }
    }

    private void registerBlock(Block block) {
        try {
            ResourcePath statePath = new ResourcePath(block.getRegistryName(), "blockstates", ".json");
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
        }  catch (Throwable t) {
            System.out.println("Error registering block: " + block.getRegistryName());
            if (debug) {
                t.printStackTrace();
            }
        }
    }

    private void registerVariant(Block block, IBlockState variant, int meta, BlockState blockState) {
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
            System.out.println("Error registering variant: " + variant);
            if (debug) {
                t.printStackTrace();
            }
        }
    }
}
