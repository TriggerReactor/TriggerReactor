package io.github.wysohn.triggerreactor.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarUtils {
    public static void copyFolderFromJar(String folderName, File destFolder) throws IOException{
        byte[] buffer = new byte[1024];

        ZipInputStream zis = new ZipInputStream(new FileInputStream(JarUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath()));

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if(!entry.getName().startsWith(folderName+"/"))
                continue;

            String fileName = entry.getName();

            File file = new File(destFolder + File.separator + fileName);

            FileOutputStream fos = new FileOutputStream(file);

            int len;
            while((len = zis.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        zis.closeEntry();
        zis.close();
    }
}
