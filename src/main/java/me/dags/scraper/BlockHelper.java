package me.dags.scraper;

import me.dags.scraper.adapter.Adapter;
import me.dags.scraper.adapter.BlockAdapter;
import me.dags.scraper.adapter.StairAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import org.dynmap.modsupport.ModSupportAPI;
import org.dynmap.modsupport.ModTextureDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class BlockHelper {

    private static final Map<Class<?>, Adapter> CONVERTERS = new HashMap<Class<?>, Adapter>(){{
        put(Block.class, new BlockAdapter());
        put(BlockStairs.class, new StairAdapter());
    }};

    private static ModTextureDefinition getDefinition(String domain) {
        return ModSupportAPI.getAPI().getModTextureDefinition(domain, "1.10.2");
    }

    public static void convert() {
        Set<String> domains = new HashSet<>();
        for (Block block : Block.REGISTRY) {
            if (!block.getRegistryName().getResourceDomain().equals("minecraft")) {
                convert(block);
                domains.add(block.getRegistryName().getResourceDomain());
            }
        }

        for (String domain : domains) {
            getDefinition(domain).publishDefinition();
            getDefinition(domain).getModelDefinition().publishDefinition();
        }
    }

    public static void convert(Block block) {
        Class<?> clazz = block.getClass();
        Adapter adapter = null;
        while (clazz != null && clazz != Object.class && (adapter = CONVERTERS.get(clazz)) == null) {
            clazz = clazz.getSuperclass();
        }

        if (adapter != null) {
            String name = block.getRegistryName().getResourcePath();
            ModTextureDefinition definition = getDefinition(block.getRegistryName().getResourceDomain());
            Set<Integer> visited = new HashSet<>();
            for (IBlockState blockState : block.getBlockState().getValidStates()) {
                int meta = block.getMetaFromState(blockState);
                if (visited.add(meta)) {
                    adapter.apply(definition, name, blockState, meta);
                }
            }
        }
    }
}
