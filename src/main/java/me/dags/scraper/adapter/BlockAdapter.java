package me.dags.scraper.adapter;

import org.dynmap.modsupport.BoxBlockModel;
import org.dynmap.modsupport.ModModelDefinition;

/**
 * @author dags <dags@dags.me>
 */
public class BlockAdapter extends Adapter<BoxBlockModel> {
    @Override
    BoxBlockModel getModel(ModModelDefinition definition, String name) {
        return definition.addBoxModel(name);
    }
}
