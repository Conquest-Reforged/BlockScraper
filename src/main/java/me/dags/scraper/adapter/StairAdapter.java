package me.dags.scraper.adapter;

import org.dynmap.modsupport.ModModelDefinition;
import org.dynmap.modsupport.StairBlockModel;

/**
 * @author dags <dags@dags.me>
 */
public class StairAdapter extends Adapter<StairBlockModel> {
    @Override
    StairBlockModel getModel(ModModelDefinition definition, String name) {
        return definition.addStairModel(name);
    }
}
