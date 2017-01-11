package me.dags.scraper.asset.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Model {

    public final Map<String, String> textures = new HashMap<>();
    public final List<Element> elements = new ArrayList<>();

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
}
