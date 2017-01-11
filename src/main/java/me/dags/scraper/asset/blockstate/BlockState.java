package me.dags.scraper.asset.blockstate;

import me.dags.scraper.asset.AssetUtils;
import me.dags.scraper.asset.model.Model;
import me.dags.scraper.asset.util.ResourcePath;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class BlockState {

    public final Map<String, Variant> variants = new HashMap<>();

    public Model getModel(String blockstateQuery) {
        Variant variant = variants.get(blockstateQuery);
        if (variant != null) {
            ResourcePath modelPath = new ResourcePath(variant.model, "models/block", ".json");
            Model model = AssetUtils.getModel(modelPath);
            if (!variant.axis.isEmpty()) {
                if (variant.axis.equals("x")) {
                    return model.rotateX(variant.rotation);
                }
                if (variant.axis.equals("y")) {
                    return model.rotateY(variant.rotation);
                }
                if (variant.axis.equals("z")) {
                    return model.rotateZ(variant.rotation);
                }
            } else {
                return model;
            }
        }
        return null;
    }
}
