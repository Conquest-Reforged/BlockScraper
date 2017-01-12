package me.dags.scraper;

import me.dags.scraper.asset.AssetContainer;
import me.dags.scraper.asset.AssetManager;
import me.dags.scraper.asset.blockstate.BlockState;
import me.dags.scraper.asset.model.Model;
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
        findAssets();
        scrapeBlocks();
    }

    private void findAssets() {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            AssetContainer container = new AssetContainer(mod.getModId(), mod.getSource());
            if (mod.getModId().equals(MOD_ID)) {
                AssetManager.getInstance().setDefaultContainer(container);
            } else {
                AssetManager.getInstance().addContainer(container);
            }
        }
    }

    private void scrapeBlocks() {
        for (Block block : Block.REGISTRY) {
            if (!block.getRegistryName().getResourceDomain().equals("minecraft")) {
                registerBlock(block);
            }
        }
        AssetManager.getInstance().clear();
        ModelRegistrar.publish();
        ModelRegistrar.clear();
    }

    private void registerBlock(Block block) {
        ResourceLocation registryName = block.getRegistryName();
        String domain = registryName.getResourceDomain();
        String blockName = registryName.getResourcePath();
        ResourcePath statePath = new ResourcePath(registryName.toString(), "blockstates", ".json");

        BlockState blockState = BlockState.forPath(statePath);
        if (blockState != null && !blockState.hasVariants()) {
            Set<Integer> visited = new HashSet<>();

            for (IBlockState variant : block.getBlockState().getValidStates()) {
                int meta = block.getMetaFromState(variant);
                if (visited.add(meta)) {
                    try {
                        String query = StateMapper.getBlockstateStateQuery(variant);
                        Model model = blockState.getModel(query);
                        ModelRegistrar.register(domain, blockName, meta, model);
                    } catch (Throwable throwable) {
                        System.out.println("Error on block: " + registryName);
                        if (debug) {
                            throwable.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
