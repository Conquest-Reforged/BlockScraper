package me.dags.scraper.asset.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author dags <dags@dags.me>
 */
public class Variant {

    final String model;
    final String axis;
    final int rotation;

    public Variant(JsonObject variant) {
        JsonElement model = variant.get("model");
        JsonElement x = variant.get("z");
        JsonElement y = variant.get("y");
        JsonElement z = variant.get("z");
        if (model != null) {
            this.model = model.getAsString();
        } else {
            this.model = "";
        }
        if (x != null) {
            this.axis = "x";
            this.rotation = x.getAsInt();
        } else if (y != null) {
            this.axis = "y";
            this.rotation = y.getAsInt();
        } else if (z != null) {
            this.axis = "z";
            this.rotation = z.getAsInt();
        } else {
            this.axis = "";
            this.rotation = 0;
        }
    }
}
