package me.dags.scraper.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dags.scraper.asset.util.ResourcePath;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public final class AssetManager {

    private static final AssetManager instance = new AssetManager();

    private AssetContainer defaultContainer;
    private final List<AssetContainer> containers = new ArrayList<>();
    private final Map<ResourcePath, JsonObject> resourceCache = new HashMap<>();

    private AssetManager() {
        defaultContainer = new AssetContainer("default", new File(new File("").getAbsolutePath()));
    }

    public static AssetManager getInstance() {
        return instance;
    }

    public void clear() {
        containers.clear();
        resourceCache.clear();
        defaultContainer = null;
    }

    public void setDefaultContainer(AssetContainer container) {
        this.defaultContainer = container;
    }

    public void addContainer(AssetContainer container) {
        this.containers.add(container);
    }

    public JsonObject getJson(ResourcePath path) {
        // Check if resource has been accessed before
        JsonObject cached = resourceCache.get(path);
        if (cached != null) {
            return cached;
        }

        ResourcePath resource = path.withExtension(".json");
        try (InputStream inputStream = getResource(resource)) {
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                JsonElement element = new JsonParser().parse(reader);
                JsonObject object = element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();

                // Cache in-case the resource needs to be accessed again
                resourceCache.put(path, object);

                return object;
            }
        } catch (IOException e) {
            return new JsonObject();
        }
    }

    public void extractToDir(File dir, ResourcePath resource) {
        File out = new File(dir, resource.getFilePath());
        if (out.exists()) {
            return;
        }
        try (InputStream inputStream = getResource(resource)) {
            out.getParentFile().mkdirs();
            Files.copy(inputStream, out.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InputStream getResource(ResourcePath path) throws FileNotFoundException {
        InputStream inputStream = null;
        for (int i = containers.size() - 1; inputStream == null && i > -1; i--) {
            try {
                AssetContainer container = containers.get(i);
                inputStream = container.getInputStream(path);
            } catch (IOException e) {}
        }

        if (inputStream != null) {
            return inputStream;
        }

        try {
            return defaultContainer.getInputStream(path);
        } catch (IOException e) {
            throw new FileNotFoundException(path.toString());
        }
    }
}
