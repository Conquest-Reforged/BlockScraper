package me.dags.scraper.asset.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dags <dags@dags.me>
 */
public class ResourcePath {

    private final String domain;
    private final String fileName;
    private final String filePath;
    private final String extension;
    private final int hash;

    public ResourcePath(String in, String parent) {
        this(in, parent, "");
    }

    public ResourcePath(String in, String require, String extension) {
        int index = in.indexOf(':');
        int start = index + 1;
        int end = in.endsWith(extension) ? in.length() - extension.length() : in.length();

        String domain = index > 0 ? in.substring(0, index) : "minecraft";
        this.domain = domain;

        Path resource = Paths.get(start < end ? in.substring(start, end) : in);
        this.fileName = resource.getFileName().toString();

        Path parent = Paths.get(require);
        Path path = parent.resolveSibling(resource);
        if (path.getNameCount() == parent.getNameCount()) {
            path = parent.resolve(resource);
        }

        Path absFile = Paths.get("assets").resolve(domain).resolve(path);
        this.filePath = toString(absFile);
        this.extension = extension;
        this.hash = getUniqueName().hashCode();
    }

    public ResourcePath(String domain, String fileName, String filePath, String extension) {
        this.domain = domain;
        this.fileName = fileName;
        this.filePath = filePath;
        this.extension = extension;
        this.hash = filePath.hashCode();
    }

    public ResourcePath withExtension(String extension) {
        if (this.extension.equals(extension)) {
            return this;
        }
        return new ResourcePath(domain, fileName, filePath, extension);
    }

    public String getUniqueName() {
        return domain + ":" + getFilePath();
    }

    public String getResourceName() {
        return fileName;
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
        return hash;
    }

    private static String toString(Path path) {
        StringBuilder builder = new StringBuilder();
        for (Path node : path) {
            builder.append(builder.length() > 0 ? "/" : "").append(node.getFileName());
        }
        return builder.toString();
    }
}
