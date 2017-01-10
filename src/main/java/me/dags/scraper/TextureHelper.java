package me.dags.scraper;

import me.dags.scraper.resource.TextureLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.dynmap.modsupport.ModTextureDefinition;
import org.dynmap.modsupport.TextureFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class TextureHelper {

    private static final Map<TextureLocation, TextureFile> textures = new HashMap<>();

    public static TextureFile getTexture(ModTextureDefinition definition, String texture) {
        TextureLocation textureLocation = new TextureLocation(texture);
        TextureFile textureFile = textures.get(textureLocation);
        if (textureFile == null) {
            textureFile = definition.registerTextureFile(textureLocation.getId(), textureLocation.getFilePath());
            textures.put(textureLocation, textureFile);
        }
        return textureFile;
    }

    public static void copyTextures(Path texturePacks) {
        for (TextureLocation texture : textures.keySet()) {
            Path out = texturePacks.resolve(texture.getFilePath());
            if (Files.exists(out)) {
                return;
            }
            try {
                Files.createDirectories(out.getParent());
                ResourceLocation location = new ResourceLocation(texture.getDomain(), texture.getResourcePath() + ".png");

                // Not server friendly
                try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location)) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        Files.copy(inputStream, out);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void clear() {
        textures.clear();
    }
}
