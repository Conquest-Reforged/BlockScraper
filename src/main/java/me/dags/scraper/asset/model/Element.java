package me.dags.scraper.asset.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Element {

    public final int x1, y1, z1;
    public final int x2, y2, z2;
    public final List<String> faces = new ArrayList<>();

    private Element(int x1, int y1, int z1, int x2, int y2, int z2, List<String> faces) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
        this.z2 = Math.max(z1, z2);
        this.faces.addAll(faces);
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
                for (Map.Entry<String, JsonElement> face : faces.entrySet()) {
                    this.faces.add(face.getKey());
                }
            }
        } else {
            throw new UnsupportedOperationException("Invalid Element! from:" + from + " to:" + to + " faces:" + faces);
        }
    }

    public Element rotateX(int degrees) {
        double rads = Math.toRadians(degrees);
        int yOff = degrees > 89 && degrees < 271 ? 16 : 0;
        int zOff = degrees > 179 && degrees < 361 ? 16 : 0;
        int y1 = (int) Math.round(this.y1 * Math.cos(rads) - this.z1 * Math.sin(rads)) + yOff;
        int z1 = (int) Math.round(this.z1 * Math.cos(rads) + this.y1 * Math.sin(rads)) + zOff;
        int y2 = (int) Math.round(this.y2 * Math.cos(rads) - this.z2 * Math.sin(rads)) + yOff;
        int z2 = (int) Math.round(this.z2 * Math.cos(rads) + this.y2 * Math.sin(rads)) + zOff;
        return new Element(x1, y1, z1, x2, y2, z2, faces);
    }

    public Element rotateY(int degrees) {
        double rads = Math.toRadians(degrees);
        int xOff = degrees > 89 && degrees < 270 ? 16 : 0;
        int zOff = degrees > 179 && degrees < 360 ? 16 : 0;
        int x1 = (int) Math.round(this.x1 * Math.cos(rads) - this.z1 * Math.sin(rads)) + xOff;
        int z1 = (int) Math.round(this.z1 * Math.cos(rads) + this.x1 * Math.sin(rads)) + zOff;
        int x2 = (int) Math.round(this.x2 * Math.cos(rads) - this.z2 * Math.sin(rads)) + xOff;
        int z2 = (int) Math.round(this.z2 * Math.cos(rads) + this.x2 * Math.sin(rads)) + zOff;
        return new Element(x1, y1, z1, x2, y2, z2, faces);
    }

    public Element rotateZ(int degrees) {
        double rads = Math.toRadians(degrees);
        int xOff = degrees > 89 && degrees < 271 ? 16 : 0;
        int yOff = degrees > 179 && degrees < 361 ? 16 : 0;
        int x1 = (int) Math.round(this.x1 * Math.cos(rads) - this.y1 * Math.sin(rads)) + xOff;
        int y1 = (int) Math.round(this.y1 * Math.cos(rads) + this.x1 * Math.sin(rads)) + yOff;
        int x2 = (int) Math.round(this.x2 * Math.cos(rads) - this.y2 * Math.sin(rads)) + xOff;
        int y2 = (int) Math.round(this.y2 * Math.cos(rads) + this.x2 * Math.sin(rads)) + yOff;
        return new Element(x1, y1, z1, x2, y2, z2, faces);
    }

    @Override
    public String toString() {
        return String.format("from=(%s,%s,%s), to=(%s,%s,%s), faces=%s", x1, y1, z1, x2, y2, z2, faces);
    }
}
