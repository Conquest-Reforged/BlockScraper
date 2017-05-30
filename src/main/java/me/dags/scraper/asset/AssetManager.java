package me.dags.scraper.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dags.scraper.BlockScraper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author dags <dags@dags.me>
 */
public final class AssetManager {

    private static final AssetManager INSTANCE = new AssetManager();
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final JsonObject EMPTY_OBJ = new JsonObject();
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String REGEX = "\\bassets\\b(.*?)\\b(blockstates|models)\\b";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private AssetPack assets = AssetPack.of(PATTERN);
    private Collection<String> domains = Collections.emptyList();
    private Map<AssetPath, JsonObject> jsonCache = Collections.emptyMap();

    public static AssetManager getInstance() {
        return INSTANCE;
    }

    public void clear() {
        assets.clear();
        domains.clear();
        jsonCache.clear();
        assets = null;
        domains = null;
        jsonCache = null;
    }

    public void findAssets() {
        Path defaultPack = Paths.get("");
        List<Path> sources = new ArrayList<>();
        List<String> domains = new ArrayList<>();
        domains.add("minecraft");

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            domains.add(mod.getModId());

            if (mod.getModId().equals(BlockScraper.MOD_ID)) {
                // Add self as the root AssetContainer (other mods override me)
                defaultPack = mod.getSource().toPath();
            } else {
                // Add other to containers list
                sources.add(mod.getSource().toPath());
            }
        }

        if (!defaultPack.toString().equals("")) {
            sources.add(defaultPack);
        }

        this.assets = AssetPack.of(PATTERN, sources);
        this.jsonCache = new HashMap<>();
        this.domains = new ArrayList<>(domains);
    }

    public JsonObject getJson(AssetPath path) {
        JsonObject cached = jsonCache.get(path = path.withExtension(".json"));
        if (cached != null) {
            return cached;
        }

        byte[] data = assets.getBytes(path);
        if (data.length > 0) {
            String json = new String(data, UTF_8);
            JsonElement element = JSON_PARSER.parse(json);
            JsonObject object = element.isJsonObject() ? element.getAsJsonObject() : EMPTY_OBJ;
            jsonCache.put(path, object);
            return object;
        } else {
            return EMPTY_OBJ;
        }
    }

    public void extractAssets(Function<String, AssetPath> pathFunction, Path root) {
        for (String domain : domains) {
            AssetPath path = pathFunction.apply(domain);
            extractAssets(path, root);
        }
    }

    public void extractAssets(AssetPath match, Path root) {
        assets.transferChildren(match, root);
    }
}
