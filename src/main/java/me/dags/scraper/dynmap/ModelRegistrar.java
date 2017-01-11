package me.dags.scraper.dynmap;

import me.dags.scraper.asset.AssetManager;
import me.dags.scraper.asset.model.Element;
import me.dags.scraper.asset.model.Model;
import me.dags.scraper.asset.util.ResourcePath;
import net.minecraftforge.fml.common.Loader;
import org.dynmap.modsupport.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class ModelRegistrar {

    private static Map<String, ModTextureDefinition> definitions = new HashMap<>();
    private static File textureDir;

    public static void publish() {
        for (ModTextureDefinition definition : definitions.values()) {
            definition.getModelDefinition().publishDefinition();
            definition.publishDefinition();
        }
        definitions.clear();
    }

    public static void register(String domain, String name, int meta, Model model) {
        ModTextureDefinition definition = getDefinition(domain);

        BlockTextureRecord textureRecord = definition.addBlockTextureRecord(name);
        textureRecord.setMetaValue(meta);

        CuboidBlockModel blockModel = definition.getModelDefinition().addCuboidModel(name);
        blockModel.setMetaValue(meta);

        registerShapes(blockModel, model);
        registerTextures(definition, textureRecord, model);
    }

    private static ModTextureDefinition getDefinition(String domain) {
        ModTextureDefinition definition = definitions.get(domain);
        if (definition == null) {
            definitions.put(domain, definition = ModSupportAPI.getAPI().getModTextureDefinition(domain, "1.0"));
        }
        return definition;
    }

    private static void registerTextures(ModTextureDefinition definition, BlockTextureRecord textureRecord, Model model) {
        for (Map.Entry<String, String> entry : model.textures.entrySet()) {
            BlockSide side = side(entry.getKey());
            String icon = entry.getValue();

            if (icon.startsWith("#")) {
                icon = model.textures.get(icon.substring(1));
            }

            if (icon != null) {
                ResourcePath texture = new ResourcePath(icon, "textures/blocks", ".png");
                TextureFile textureFile = definition.registerTextureFile(texture.getResourceName(), texture.getFilePath());
                textureRecord.setSideTexture(textureFile, side);

                AssetManager.getInstance().extractToDir(getTextureDir(), texture);
            }
        }
    }

    private static void registerShapes(CuboidBlockModel blockModel, Model model) {
        for (Element element : model.elements) {
            Cuboid.Builder builder = new Cuboid.Builder();

            builder.min(element.x1, element.y1, element.z1);
            builder.max(element.x2, element.y2, element.z2);

            for (String face : element.faces) {
                BlockSide side = side(face);
                if (side != null) {
                    builder.side(side);
                }
            }

            builder.build().addToModel(blockModel);
        }
    }

    private static File getTextureDir() {
        if (textureDir == null) {
            File root = Loader.instance().getConfigDir().getParentFile();
            return textureDir = new File(root, "dynmap/texturepacks/standard");
        }
        return textureDir;
    }

    private static BlockSide side(String in) {
        switch (in) {
            case "down":
            case "bottom":
                return BlockSide.BOTTOM;
            case "up":
            case "top":
                return BlockSide.TOP;
            case "north":
                return BlockSide.NORTH;
            case "south":
                return BlockSide.SOUTH;
            case "east":
                return BlockSide.EAST;
            case "west":
                return BlockSide.WEST;
            case "side":
            case "sides":
                return BlockSide.ALLSIDES;
        }
        return BlockSide.ALLSIDES;
    }
}
