package io.github.mtykk.luckygames;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

public class FileHelper {
    public static void unzip(InputStream source, File target) throws IOException {
        ZipInputStream sourceStream = new ZipInputStream(source);
        ZipEntry sourceZipEntry = sourceStream.getNextEntry();

        byte[] buffer = new byte[1024];

        while(sourceZipEntry != null){
            File newFile = new File(target,sourceZipEntry.getName());
            if(!newFile.getCanonicalPath().startsWith(target.getCanonicalPath()+File.separator)){
                throw new IOException("Bad zip entry, skipping");
            }

            if(sourceZipEntry.isDirectory()){
                if(!newFile.isDirectory() && !newFile.mkdirs()){
                    throw new IOException("Failed to mkdirs");
                }
            }else{
                File parent = new File(newFile.getParent());
                if(!parent.isDirectory() && !parent.mkdirs()){
                    throw new IOException("Failed to mkdirs");
                }
                try(FileOutputStream newFileOutPutStream = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = sourceStream.read(buffer)) > 0) {
                        newFileOutPutStream.write(buffer, 0, len);
                    }
                }
            }
            sourceZipEntry = sourceStream.getNextEntry();
        }

        sourceStream.closeEntry();
        sourceStream.close();
    }

    public static boolean deleteDir(File target){
        try{
            FileUtils.deleteDirectory(target);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
