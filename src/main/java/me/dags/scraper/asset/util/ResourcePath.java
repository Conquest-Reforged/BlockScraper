package me.dags.scraper.asset.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dags <dags@dags.me>
 */
public class ResourcePath {

    private final String id;
    private final String domain;
    private final String resource;
    private final String fileName;
    private final String filePath;
    private final String extension;

    public ResourcePath(String in, String parent) {
        this(in, parent, "");
    }

    public ResourcePath(String in, String require, String extension) {
        int index = in.indexOf(':');
        int start = index + 1;
        int end = in.length();

        this.id = in;

        String domain = index > 0 ? in.substring(0, index) : "minecraft";
        this.domain = domain;

        Path resource = Paths.get(start < end ? in.substring(start, end) : in);
        this.fileName = resource.getFileName().toString();

        Path parent = Paths.get(require);
        Path path = parent.resolveSibling(resource);
        if (path.getNameCount() == parent.getNameCount()) {
            path = parent.resolve(resource);
        }

        this.resource = toString(path);

        Path absFile = Paths.get("assets").resolve(domain).resolve(path);
        this.filePath = toString(absFile);

        this.extension = extension;
    }

    public ResourcePath(String id, String domain, String resource, String file, String filePath, String extension) {
        this.id = id;
        this.domain = domain;
        this.resource = resource;
        this.fileName = file;
        this.filePath = filePath;
        this.extension = extension;
    }

    public ResourcePath withExtension(String extension) {
        if (this.extension.equals(extension)) {
            return this;
        }
        return new ResourcePath(id, domain, resource, fileName, filePath, extension);
    }

    public String getId() {
        return domain + ":" + resource;
    }

    public String getResourceName() {
        return fileName;
    }

    public String getFileName() {
        return fileName + extension;
    }

    public String getResourcePath() {
        return resource + extension;
    }

    public String getFilePath() {
        return filePath + extension;
    }

    @Override
    public String toString() {
        return domain + ":" + filePath;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof ResourcePath && other.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    private static String toString(Path path) {
        StringBuilder builder = new StringBuilder();
        for (Path node : path) {
            builder.append(builder.length() > 0 ? "/" : "").append(node.getFileName());
        }
        return builder.toString();
    }
}
