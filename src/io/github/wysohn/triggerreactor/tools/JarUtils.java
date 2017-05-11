/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarUtils {
    public static void copyFolderFromJar(String folderName, File destFolder) throws IOException{
        byte[] buffer = new byte[1024];

        ZipInputStream zis = new ZipInputStream(new FileInputStream(
                URLDecoder.decode(JarUtils.class.getProtectionDomain().getCodeSource().getLocation().toString(), "UTF-8")));

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
