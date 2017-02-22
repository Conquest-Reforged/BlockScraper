package me.dags.scraper.asset.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.dags.scraper.asset.AssetManager;
import me.dags.scraper.asset.AssetPath;
import me.dags.scraper.asset.model.Model;
import me.dags.scraper.asset.model.ModelType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */

// Representation of a blockstate json file
public class BlockState {

    private final ModelType modelType;
    private final Map<String, Variant> variants = new HashMap<>();

    private BlockState(JsonObject object) {
        JsonElement model = object.get("dyn_model");

        if (model != null) {
            modelType = ModelType.forName(model.getAsString());
        } else {
            modelType = ModelType.CUSTOM;
        }

        JsonObject variants = object.getAsJsonObject("variants");
        if (variants != null) {
            for (Map.Entry<String, JsonElement> variant : variants.entrySet()) {
                if (variant.getValue().isJsonObject()) {
                    this.variants.put(variant.getKey(), new Variant(variant.getValue().getAsJsonObject()));
                }
            }
        }
    }

    public ModelType getModelType() {
        return modelType;
    }

    public boolean hasVariants() {
        return !variants.isEmpty();
    }

    public Model getModel(String blockstateQuery) {
        Variant variant = variants.get(blockstateQuery);
        if (variant != null) {
            AssetPath modelPath = AssetPath.of(variant.model, "models/block");
            Model model = Model.forPath(modelPath);

            if (variant.x != 0) {
                model = model.rotateX(variant.x);
            }
            if (variant.y != 0) {
                model = model.rotateY(variant.y);
            }
            if (variant.z != 0) {
                model = model.rotateZ(variant.z);
            }

            return model;
        }
        return null;
    }

    public static BlockState forPath(AssetPath path) {
        JsonObject object = AssetManager.getInstance().getJson(path);
        return new BlockState(object);
    }
}
