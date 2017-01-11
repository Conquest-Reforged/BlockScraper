package me.dags.scraper.asset;

import me.dags.scraper.asset.util.ResourcePath;
import me.dags.scraper.asset.util.ZipStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author dags <dags@dags.me>
 */
public class AssetContainer {

    private final File container;
    private final String id;

    public AssetContainer(String id, File file) {
        this.container = file;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public InputStream getInputStream(ResourcePath resourcePath) throws IOException {
        if (container.isDirectory()) {
            File file = new File(container, resourcePath.getFilePath());
            return new FileInputStream(file);
        } else {
            ZipFile zipFile = new ZipFile(container);
            ZipEntry entry = zipFile.getEntry(resourcePath.getFilePath());
            if (entry != null) {
                return new ZipStream(zipFile, entry);
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return id + ":" + container;
    }
}
