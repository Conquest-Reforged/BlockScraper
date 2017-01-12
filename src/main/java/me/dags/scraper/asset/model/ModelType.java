package me.dags.scraper.asset.model;

/**
 * @author dags <dags@dags.me>
 */
public enum ModelType {
    CUSTOM,
    DOOR,
    FENCE,
    PANE,
    PLANT,
    WALL,
    ;

    public static ModelType forName(String name) {
        switch (name) {
            case "door":
                return DOOR;
            case "fence":
                return FENCE;
            case "plant":
                return PLANT;
            case "wall":
                return WALL;
            default:
                return CUSTOM;
        }
    }
}
