package me.dags.scraper.asset.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author dags <dags@dags.me>
 */
public class Variant {

    final String model;
    final int x;
    final int y;
    final int z;

    public Variant(JsonObject variant) {
        JsonElement model = variant.get("model");
        JsonElement x = variant.get("x");
        JsonElement y = variant.get("y");
        JsonElement z = variant.get("z");
        this.model = model != null ? model.getAsString() : "";
        this.x = x != null ? x.getAsInt() : 0;
        this.y = y != null ? y.getAsInt() : 0;
        this.z = z != null ? z.getAsInt() : 0;
    }
}
