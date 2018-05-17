package me.dags.scraper;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    private boolean scrape = true;
    private Set<String> mods = new HashSet<>();

    public boolean doScrape() {
        return scrape;
    }

    public boolean hasAll(Collection<String> mods) {
        return mods.containsAll(mods);
    }

    public static Config read(File file) {
        Config config = new Config();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JsonElement json = new JsonParser().parse(reader);
                if (json.isJsonObject()) {
                    config.scrape = json.getAsJsonObject().get("scrape").getAsBoolean();
                    for (JsonElement e : json.getAsJsonObject().getAsJsonArray("mods")) {
                        config.mods.add(e.getAsString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    public static void write(File file, Collection<String> blockMods) {
        JsonObject config = new JsonObject();
        config.addProperty("scrape", false);

        JsonArray mods = new JsonArray();
        blockMods.forEach(mods::add);
        config.add("mods", mods);

        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Collection<String> findBlockProvidingMods() {
        Set<String> mods = new HashSet<>();
        for (ResourceLocation location : Block.REGISTRY.getKeys()) {
            String domain = location.getResourceDomain();
            ModContainer container = Loader.instance().getIndexedModList().get(domain);
            if (container == null) {
                continue;
            }
            String mod = container.getModId() + "-" + container.getVersion();
            mods.add(mod);
        }
        return mods;
    }
}
