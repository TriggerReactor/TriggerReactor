package io.github.wysohn.triggerreactor.core.manager.config.source;

import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public class ConfigSourceFactory {

    /**
     * Create empty datasource. This is useful if the Trigger do not have config file.
     *
     * @param folder
     * @param fileName
     * @return the datasource which does nothing.
     */
    public static IConfigSource none(File folder, String fileName) {
        return new EmptyConfigSource();
    }

    /**
     * Create empty datasource. This is useful if the Trigger do not have config file.
     *
     * @param file
     * @return the datasource which does nothing.
     */
    public static IConfigSource none(File file) {
        return new EmptyConfigSource();
    }

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

        if (!fileName.endsWith(".json"))
            fileName = fileName + ".json";

        return new GsonConfigSource(new File(folder, fileName));
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

    private static class EmptyConfigSource implements IConfigSource {
        @Override
        public <T> Optional<T> get(String key, Class<T> asType) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> get(String key) {
            return Optional.empty();
        }

        @Override
        public void put(String key, Object value) {

        }

        @Override
        public boolean has(String key) {
            return false;
        }

        @Override
        public Set<String> keys() {
            return null;
        }

        @Override
        public boolean isSection(String key) {
            return false;
        }

        @Override
        public void reload() {

        }

        @Override
        public void saveAll() {

        }

        @Override
        public void disable() {

        }

        @Override
        public void delete() {

        }
    }
}
