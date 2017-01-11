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
        SideUtil counter = new SideUtil();

        for (Map.Entry<String, String> entry : model.textures.entrySet()) {
            BlockSide side = SideUtil.fromName(entry.getKey());
            String icon = entry.getValue();

            if (icon.startsWith("#")) {
                icon = model.textures.get(icon.substring(1));
            }

            if (icon != null) {
                ResourcePath texture = new ResourcePath(icon, "textures/blocks", ".png");
                TextureFile textureFile = definition.registerTextureFile(texture.getResourceName(), texture.getFilePath());
                textureRecord.setSideTexture(textureFile, side);

                counter.recordSide(side, textureFile);
                AssetManager.getInstance().extractToDir(getTextureDir(), texture);
            }
        }

        if (counter.hasFallback()) {
            TextureFile fallback = counter.getFallbackTexture();
            for (BlockSide side : counter.getMissingSides()) {
                textureRecord.setSideTexture(fallback, side);
            }
        }
    }

    private static void registerShapes(CuboidBlockModel blockModel, Model model) {
        for (Element element : model.elements) {
            double x1 = element.x1 / 16D;
            double y1 = element.y1 / 16D;
            double z1 = element.z1 / 16D;
            double x2 = element.x2 / 16D;
            double y2 = element.y2 / 16D;
            double z2 = element.z2 / 16D;
            double xmin = Math.min(x1, x2);
            double xmax = Math.max(x1, x2);
            double ymin = Math.min(y1, y2);
            double ymax = Math.max(y1, y2);
            double zmin = Math.min(z1, z2);
            double zmax = Math.max(z1, z2);
            blockModel.addCuboid(xmin, ymin, zmin, xmax, ymax, zmax, null);
        }
    }

    private static File getTextureDir() {
        if (textureDir == null) {
            File root = Loader.instance().getConfigDir().getParentFile();
            return textureDir = new File(root, "dynmap/texturepacks/standard");
        }
        return textureDir;
    }
}
