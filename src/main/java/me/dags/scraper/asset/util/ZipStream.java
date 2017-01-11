package me.dags.scraper.asset.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author dags <dags@dags.me>
 */
public class ZipStream extends InputStream implements AutoCloseable {

    private final ZipFile zipFile;
    private final InputStream inputStream;

    public ZipStream(ZipFile zipFile, ZipEntry entry) throws IOException {
        this.zipFile = zipFile;
        this.inputStream = zipFile.getInputStream(entry);
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        super.close();
        inputStream.close();
        zipFile.close();
    }
}
