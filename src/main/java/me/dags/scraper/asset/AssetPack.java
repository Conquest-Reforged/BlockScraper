package me.dags.scraper.asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author dags <dags@dags.me>
 */
public class AssetPack {

    private static final byte[] EMPTY = new byte[0];

    private final Map<AssetPath, byte[]> assets;
    private final int bytes;

    private AssetPack(Builder builder) {
        this.assets = new HashMap<>(builder.assets);
        this.bytes = builder.bytes.get();
    }

    public void clear() {
        assets.clear();
    }

    public Stream<AssetPath> getEntries() {
        return assets.keySet().stream();
    }

    public Stream<AssetPath> matchChildren(AssetPath path) {
        return assets.keySet().stream().filter(p -> p.isChildOf(path));
    }

    public void transferChildren(AssetPath path, Path root) {
        matchChildren(path).forEach(match -> {
            try {
                transfer(match, root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void transfer(AssetPath path, Path rootDir) throws IOException {
        byte[] data = assets.get(path);
        if (data != EMPTY) {
            Path output = path.merge(rootDir);
            Files.createDirectories(output.getParent());
            try (OutputStream outputStream = Files.newOutputStream(output)) {
                outputStream.write(data);
                outputStream.flush();
            }
        }
    }

    public byte[] getBytes(AssetPath path) {
        return assets.getOrDefault(path, EMPTY);
    }

    public int getCount() {
        return assets.size();
    }

    public int getSize() {
        return bytes;
    }

    public static AssetPack of(Pattern filter, Path... containers) {
        return of(filter, Arrays.asList(containers));
    }

    public static AssetPack of(Pattern filter, List<Path> containers) {
        Builder builder = new Builder(filter);
        for (Path file : containers) {
            builder.add(file);
        }
        return new AssetPack(builder);
    }

    public static class Builder {

        private final Map<AssetPath, byte[]> assets = new HashMap<>();
        private final AtomicInteger bytes = new AtomicInteger(0);
        private final Pattern filter;

        private Builder(Pattern pattern) {
            this.filter = pattern;
        }

        private void add(Path path) {
            if (Files.isDirectory(path)) {
                addFile(path);
            } else {
                addZip(path);
            }
        }

        private void addFile(Path path) {
            if (Files.isDirectory(path)) {
                try {
                    Files.walk(path).forEach(this::addFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                addFileEntry(path);
            }
        }
        private void addZip(Path file) {
            try (ZipFile zip = new ZipFile(file.toFile())) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    addZipEntry(zip, entries.nextElement());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void addFileEntry(Path file) {
            if (filter.matcher(file.toString()).find()) {
                AssetPath path = new AssetPath(file);
                if (!assets.containsKey(path)) {
                    try {
                        byte[] data = Files.readAllBytes(file);
                        addBytes(path, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void addZipEntry(ZipFile zip, ZipEntry entry) {
            if (!entry.isDirectory() && filter.matcher(entry.getName()).find()) {
                AssetPath path = new AssetPath(Paths.get(entry.getName()));
                if (!assets.containsKey(path)) {
                    try (InputStream inputStream = zip.getInputStream(entry)) {
                        byte[] data = readAll(inputStream);
                        addBytes(path, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void addBytes(AssetPath path, byte[] data) {
            assets.put(path, data);
            bytes.getAndAdd(data.length);
        }

        private byte[] readAll(InputStream inputStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
            byte[] bytes = new byte[2048];
            int read;
            while ((read = inputStream.read(bytes, 0, bytes.length)) != -1) {
                buffer.write(bytes, 0, read);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }
}

