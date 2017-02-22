package me.dags.scraper.asset;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author dags <dags@dags.me>
 */
public class AssetPath implements Comparable<AssetPath> {

    private final Path path;
    private final int hash;

    AssetPath(Path path) {
        this.path = path;
        this.hash = path.hashCode();
    }

    private AssetPath(String in) {
        this(new Reader(in).readParts());
    }

    private AssetPath(String domain, String in) {
        this(new Reader(domain, in).readParts());
    }

    private AssetPath(String... parts) {
        this(Paths.get("assets", parts));
    }

    public AssetPath resolve(String... parts) {
        return new AssetPath(merge(path, Paths.get("", parts)));
    }

    public AssetPath withExtension(String extension) {
        String name = path.getFileName().toString();
        if (!name.endsWith(extension)) {
            int lastIndex = path.getNameCount() - 1;
            if (lastIndex < 2) {
                return new AssetPath(Paths.get(name + extension));
            }
            return new AssetPath(path.subpath(0, lastIndex).resolve(name + extension));
        }
        return this;
    }

    public String getName() {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        return index > -1 ? fileName.substring(0, index) : fileName;
    }

    public boolean isChildOf(AssetPath parent) {
        return this.path.startsWith(parent.path);
    }

    public Path merge(Path parent) {
        return merge(parent, path);
    }

    @Override
    public int compareTo(AssetPath path) {
        return this.path.compareTo(path.path);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == this.getClass() && o.hashCode() == this.hashCode();
    }

    @Override
    public String toString() {
        return path.toString();
    }

    public static AssetPath domain(String domain) {
        return new AssetPath(domain, "");
    }

    public static AssetPath of(String path) {
        return new AssetPath(path);
    }

    public static AssetPath of(Object input, String parent) {
        Reader reader = new Reader(input.toString());

        String domain = reader.domain();
        String remaining = reader.remaining();

        Path route = Paths.get("assets", domain, parent);
        Path path = AssetPath.merge(route, Paths.get(remaining));

        return new AssetPath(path);
    }

    private static Path merge(Path parent, Path child) {
        Path c = child.getName(0);
        for (int i = 0; i < parent.getNameCount(); i++) {
            Path p = parent.getName(i);
            if (p.equals(c)) {
                if (i == 0) {
                    return p.resolveSibling(child);
                } else {
                    return parent.subpath(0, i).resolve(child);
                }
            }
        }
        return parent.resolve(child);
    }

    private static class Reader {

        private final String domain;
        private final String input;
        private int pos = -1;

        private Reader(String in) {
            this.input = in;
            this.domain = "minecraft";
        }

        private Reader(String domain, String in) {
            this.input = in;
            this.domain = domain;
        }

        private String remaining() {
            return input.substring(pos + 1, input.length());
        }

        private boolean hasNext() {
            return pos + 1 < input.length();
        }

        private String[] readParts() {
            String[] parts = new String[9];
            parts[0] = domain();
            int index = 1;
            while (hasNext() && index < parts.length) {
                parts[index++] = nextPart();
            }
            return Arrays.copyOf(parts, index);
        }

        private String domain() {
            this.pos = -1;
            int pos = this.pos;
            String domain = this.domain;
            while (++pos < input.length()) {
                if (input.charAt(pos) == ':') {
                    String s = input.substring(0, pos);
                    domain = s.isEmpty() ? domain : s;
                    this.pos = pos;
                    break;
                }
            }
            return domain;
        }

        private String nextPart() {
            int start = pos + 1;
            while (++pos < input.length() && input.charAt(pos) != '/');
            return input.substring(start, pos);
        }
    }
}
