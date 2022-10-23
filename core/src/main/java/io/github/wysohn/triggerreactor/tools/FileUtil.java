/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.tools;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;

public class FileUtil {
    /**
     * @param file target file
     * @param str  string to save
     * @throws IOException
     */
    public static void writeToFile(File file, String str) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File temp = File.createTempFile("CopyOf_" + file.getName(), ".tmp", file.getParentFile());

        try (FileOutputStream fos = new FileOutputStream(temp);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");) {
            osw.write(str);
        }

        try (FileInputStream istream = new FileInputStream(temp);
             FileOutputStream ostream = new FileOutputStream(file)) {
            FileChannel src = istream.getChannel();
            FileChannel dest = ostream.getChannel();
            dest.transferFrom(src, 0, src.size());
        }

        temp.delete();
    }

    public static String readFromFile(File file) throws UnsupportedEncodingException, IOException {
        if (!file.exists())
            return null;

        StringBuilder builder = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            int read = -1;
            while ((read = isr.read()) != -1) {
                builder.append((char) read);
            }
            return builder.toString();
        }
    }

    public static String readFromStream(InputStream stream) throws UnsupportedEncodingException, IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            int read = -1;
            while ((read = isr.read()) != -1) {
                builder.append((char) read);
            }
            return builder.toString();
        }
    }

    /**
     * same as file.delete() if 'file' is file; recursively deletes all elements inside if 'file' is directory.
     *
     * @param file folder or file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            for (File f : file.listFiles()) {
                delete(f);
            }
            file.delete();
        }
    }

    /**
     * Move target folder to destination folder.
     *
     * @param folder
     * @param dest
     * @param options
     * @throws IOException
     */
    public static void moveFolder(File folder, File dest, CopyOption... options) throws IOException {
        if (folder.isFile())
            return;

        for (File target : folder.listFiles()) {
            if (target.isFile()) {
                if (!dest.exists())
                    dest.mkdirs();
                Files.move(target.toPath().normalize(), new File(dest, target.getName()).toPath().normalize(), options);
            } else {
                File destFolder = new File(dest, target.getName());
                moveFolder(target, destFolder);
            }
        }

        folder.delete();
    }
}
