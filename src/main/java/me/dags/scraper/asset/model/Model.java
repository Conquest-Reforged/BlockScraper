package me.dags.scraper.asset.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.dags.scraper.asset.AssetManager;
import me.dags.scraper.asset.AssetPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
// Representation of block model json file
public class Model {

    private final Map<String, String> textures = new HashMap<>();
    private final List<Element> elements = new ArrayList<>();

    private Model(){}

    private Model(JsonObject object) {
        JsonElement parent = object.get("parent");
        JsonElement textures = object.getAsJsonObject("textures");
        JsonElement elements = object.getAsJsonArray("elements");

        Model parentModel;
        if (parent != null) {
            AssetPath path = AssetPath.of(parent.getAsString(), "models/block");
            JsonObject parentObj = AssetManager.getInstance().getJson(path);
            parentModel = new Model(parentObj);
        } else {
            parentModel = new Model();
        }

        this.textures.putAll(parentModel.textures);
        this.elements.addAll(parentModel.elements);

        if (textures != null && textures.isJsonObject()) {
            for (Map.Entry<String, JsonElement> texture : textures.getAsJsonObject().entrySet()) {
                this.textures.put(texture.getKey(), texture.getValue().getAsString());
            }
        }

        if (elements != null && elements.isJsonArray()) {
            this.elements.clear();
            for (JsonElement element : elements.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    Element el = new Element(element.getAsJsonObject());
                    if (el.isValid()) {
                        this.elements.add(el);
                    }
                }
            }
        }
    }

    public String getTexture(String side) {
        return textures.get(side);
    }

    public Iterable<Map.Entry<String, String>> getTextures() {
        return textures.entrySet();
    }

    public Iterable<Element> getElements() {
        return elements;
    }

    public Model rotateX(int degrees) {
        Model model = new Model();
        model.textures.putAll(textures);
        for (Element element : elements) {
            model.elements.add(element.rotateX(degrees));
        }
        return model;
    }

    public Model rotateY(int degrees) {
        Model model = new Model();
        model.textures.putAll(textures);
        for (Element element : elements) {
            model.elements.add(element.rotateY(degrees));
        }
        return model;
    }

    public Model rotateZ(int degrees) {
        Model model = new Model();
        model.textures.putAll(textures);
        for (Element element : elements) {
            model.elements.add(element.rotateZ(degrees));
        }
        return model;
    }

    @Override
    public String toString() {
        return "textures=" + textures.toString() + ",elements=" + elements.toString();
    }

    public static Model forPath(AssetPath path) {
        JsonObject object = AssetManager.getInstance().getJson(path);
        return new Model(object);
    }
}
