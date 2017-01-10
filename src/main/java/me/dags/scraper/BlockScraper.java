package me.dags.scraper;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

import java.io.File;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
@Mod(modid = "blockscraper", version = "1.0")
public class BlockScraper {

    private File mcDir = new File("");

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        mcDir = event.getModConfigurationDirectory().getParentFile();
    }

    @Mod.EventHandler
    public void serverStart(FMLServerAboutToStartEvent event) {
        BlockHelper.convert();
        Path textures = mcDir.toPath().resolve("dynmap").resolve("texturepacks").resolve("standard");
        TextureHelper.copyTextures(textures);
        TextureHelper.clear();
    }
}
