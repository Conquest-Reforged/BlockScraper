package me.dags.scraper.asset.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Element {

    public final int x1, y1, z1;
    public final int x2, y2, z2;
    public final List<String> faces;

    public Element(int x1, int y1, int z1, int x2, int y2, int z2, List<String> faces) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
        this.z2 = Math.max(z1, z2);
        this.faces = new ArrayList<>(faces);
    }

    public Element(JsonObject json) {
        JsonArray from = json.getAsJsonArray("from");
        JsonArray to = json.getAsJsonArray("to");
        JsonObject faces = json.getAsJsonObject("faces");

        if (from != null && to != null && from.size() == 3 && to.size() == 3) {
            x1 = from.get(0).getAsInt();
            y1 = from.get(1).getAsInt();
            z1 = from.get(2).getAsInt();

            x2 = to.get(0).getAsInt();
            y2 = to.get(1).getAsInt();
            z2 = to.get(2).getAsInt();

            if (faces != null) {
                List<String> list = new ArrayList<>();
                for (Map.Entry<String, JsonElement> face : faces.entrySet()) {
                    list.add(face.getKey());
                }
                this.faces = list;
            } else {
                this.faces = Collections.emptyList();
            }
        } else {
            this.x1 = 0;
            this.y1 = 0;
            this.z1 = 0;
            this.x2 = 0;
            this.y2 = 0;
            this.z2 = 0;
            this.faces = Collections.emptyList();
        }
    }

    public boolean isValid() {
        return !(x1 == 0 && y1 == 0 && z1 == 0 && x2 == 0 && y2 == 0 && z2 == 0);
    }

    public Element rotateX(int degrees) {
        double rads = Math.toRadians(degrees);
        int cz = 8, cy = 8;
        int z1 = cz + (int) Math.round((this.z1 - cz) * Math.cos(rads) - (this.y1 - cy) * Math.sin(rads));
        int y1 = cy + (int) Math.round((this.y1 - cy) * Math.cos(rads) + (this.z1 - cz) * Math.sin(rads));
        int z2 = cz + (int) Math.round((this.z2 - cz) * Math.cos(rads) - (this.y2 - cy) * Math.sin(rads));
        int y2 = cy + (int) Math.round((this.y2 - cy) * Math.cos(rads) + (this.z2 - cz) * Math.sin(rads));
        return new Element(x1, y1, z1, x2, y2, z2, faces);
    }

    public Element rotateY(int degrees) {
        double rads = Math.toRadians(degrees);
        int cx = 8, cz = 8;
        int x1 = cx + (int) Math.round((this.x1 - cx) * Math.cos(rads) - (this.z1 - cz) * Math.sin(rads));
        int z1 = cz + (int) Math.round((this.z1 - cz) * Math.cos(rads) + (this.x1 - cx) * Math.sin(rads));
        int x2 = cx + (int) Math.round((this.x2 - cx) * Math.cos(rads) - (this.z2 - cz) * Math.sin(rads));
        int z2 = cz + (int) Math.round((this.z2 - cz) * Math.cos(rads) + (this.x2 - cx) * Math.sin(rads));
        return new Element(x1, y1, z1, x2, y2, z2, faces);
    }

    public Element rotateZ(int degrees) {
        double rads = Math.toRadians(degrees);
        int cx = 8, cy = 8;
        int x1 = cx + (int) Math.round((this.x1 - cx) * Math.cos(rads) - (this.z1 - cy) * Math.sin(rads));
        int y1 = cy + (int) Math.round((this.y1 - cy) * Math.cos(rads) + (this.x1 - cx) * Math.sin(rads));
        int x2 = cx + (int) Math.round((this.x2 - cx) * Math.cos(rads) - (this.z2 - cy) * Math.sin(rads));
        int y2 = cy + (int) Math.round((this.y2 - cy) * Math.cos(rads) + (this.x2 - cx) * Math.sin(rads));
        return new Element(x1, y1, z1, x2, y2, z2, faces);
    }

    @Override
    public String toString() {
        return String.format("from=(%s,%s,%s), to=(%s,%s,%s), faces=%s", x1, y1, z1, x2, y2, z2, faces);
    }
}
