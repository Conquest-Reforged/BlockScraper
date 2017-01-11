package me.dags.scraper.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dags.scraper.asset.util.ResourcePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public final class AssetManager {

    private static final AssetManager instance = new AssetManager();

    private AssetContainer defaultContainer;
    private final List<AssetContainer> containers = new ArrayList<>();

    private AssetManager() {
        defaultContainer = new AssetContainer("default", new File(new File("").getAbsolutePath()));
    }

    public static AssetManager getInstance() {
        return instance;
    }

    public void clear() {
        containers.clear();
        defaultContainer = null;
    }

    public AssetContainer getDefaultContainer() {
        return defaultContainer;
    }

    public List<AssetContainer> getContainers() {
        return new ArrayList<>(containers);
    }

    public void setDefaultContainer(AssetContainer container) {
        this.defaultContainer = container;
    }

    public void addContainer(AssetContainer container) {
        this.containers.add(container);
    }

    public void setContainers(List<AssetContainer> containers) {
        this.containers.clear();
        this.containers.addAll(containers);
    }

    public JsonObject getJson(ResourcePath path) {
        ResourcePath resource = path.withExtension(".json");
        try (InputStream inputStream = getResource(resource)) {
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                JsonElement element = new JsonParser().parse(reader);
                return element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
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

    public InputStream getResource(ResourcePath path) throws IOException {
        InputStream inputStream = null;
        for (int i = containers.size() - 1; inputStream == null && i > -1; i--) {
            try {
                AssetContainer container = containers.get(i);
                inputStream = container.getInputStream(path);
            } catch (IOException e) {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e1) {}
            }
        }

        if (inputStream != null) {
            return inputStream;
        }

        return defaultContainer.getInputStream(path);
    }
}
