package io.github.wysohn.triggerreactor.core.manager.config.source;

import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;

import java.io.File;

public class ConfigSourceFactory {
    /**
     * Create a Gson based datasource.
     *
     * @param folder   the folder where json file is located
     * @param fileName the filename (without extension)
     * @return the datasource
     * @throws RuntimeException if folder is not directory.
     */
    public static IConfigSource gson(File folder, String fileName) {
        if (!folder.isDirectory())
            throw new RuntimeException("folder must be a directory.");

        return new GsonConfigSource(new File(folder, fileName + ".json"));
    }

    /**
     * Create a Gson based datasource.
     *
     * @param file the json file
     * @return the datasource
     * @throws RuntimeException file is not file
     */
    public static IConfigSource gson(File file) {
        if (!file.isFile())
            throw new RuntimeException("it must be a file.");

        return new GsonConfigSource(file);
    }
}
