package io.github.mtykk.luckygames;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHelper {
    public static void unzip(InputStream source, File target) throws IOException {
        if (source == null) throw new IOException("Zip source is missing");
        if (target == null) throw new IOException("Zip target is missing");

        File canonicalTarget = target.getCanonicalFile();
        String targetPath = canonicalTarget.getPath() + File.separator;
        byte[] buffer = new byte[8192];

        try (ZipInputStream zip = new ZipInputStream(source)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                File output = new File(canonicalTarget, entry.getName()).getCanonicalFile();
                if (!output.getPath().startsWith(targetPath)) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    if (!output.isDirectory() && !output.mkdirs()) {
                        throw new IOException("Failed to create directory " + output);
                    }
                } else {
                    File parent = output.getParentFile();
                    if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream outputStream = new FileOutputStream(output)) {
                        int length;
                        while ((length = zip.read(buffer)) != -1) {
                            if (length > 0) outputStream.write(buffer, 0, length);
                        }
                    }
                }
                zip.closeEntry();
            }
        }
    }

    public static void deleteDir(File target) throws IOException {
        if (target == null) throw new IOException("Directory to delete is missing");
        FileUtils.deleteDirectory(target);
    }
}
