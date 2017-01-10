package me.dags.scraper.resource;

/**
 * @author dags <dags@dags.me>
 */
public class TextureLocation {

    private final String domain;
    private final String file;
    private final String filePath;
    private final int hash;

    public TextureLocation(String in) {
        int index = in.indexOf(':');
        int start = index != -1 ? index + 1 : 0;
        int end = in.length();
        this.domain = index != -1 ? in.substring(0, index) : "minecraft";
        String file = in.substring(start, end);
        if (!file.startsWith("textures/")) {
            file = "textures/" + file;
        }
        this.file = file;
        this.filePath = "assets/" + domain + "/" + file + ".png";
        this.hash = getId().hashCode();
    }

    public String getDomain() {
        return domain;
    }

    public String getResourcePath() {
        return file;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getId() {
        return domain + ":" + file;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof TextureLocation && other.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return domain + " | " + file + " | " + filePath;
    }
}
