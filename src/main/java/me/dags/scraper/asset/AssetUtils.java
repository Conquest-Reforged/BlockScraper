package me.dags.scraper.asset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.dags.scraper.asset.blockstate.BlockState;
import me.dags.scraper.asset.blockstate.Variant;
import me.dags.scraper.asset.model.Element;
import me.dags.scraper.asset.model.Model;
import me.dags.scraper.asset.util.ResourcePath;

import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class AssetUtils {

    public static BlockState getState(ResourcePath path) {
        JsonObject object = AssetManager.getInstance().getJson(path);

        JsonObject variants = object.getAsJsonObject("variants");

        BlockState blockState = new BlockState();
        if (variants != null) {
            for (Map.Entry<String, JsonElement> variant : variants.entrySet()) {
                if (variant.getValue().isJsonObject()) {
                    blockState.variants.put(variant.getKey(), new Variant(variant.getValue().getAsJsonObject()));
                }
            }
        }

        return blockState;
    }

    public static Model getModel(ResourcePath path) {
        JsonObject object = AssetManager.getInstance().getJson(path);

        JsonElement parent = object.get("parent");
        JsonElement textures = object.getAsJsonObject("textures");
        JsonElement elements = object.getAsJsonArray("elements");

        Model model;
        if (parent != null) {
            model = getModel(new ResourcePath(parent.getAsString(), "models/block", ".json"));
        } else {
            model = new Model();
        }

        if (textures != null && textures.isJsonObject()) {
            for (Map.Entry<String, JsonElement> texture : textures.getAsJsonObject().entrySet()) {
                model.textures.put(texture.getKey(), texture.getValue().getAsString());
            }
        }

        if (elements != null && elements.isJsonArray()) {
            model.elements.clear();
            for (JsonElement element : elements.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    Element el = new Element(element.getAsJsonObject());
                    model.elements.add(el);
                }
            }
        }

        return model;
    }
}
