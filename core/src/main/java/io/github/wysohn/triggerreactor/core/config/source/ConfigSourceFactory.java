package io.github.wysohn.triggerreactor.core.config.source;

import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.IConfigSourceFactory;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public class ConfigSourceFactory implements IConfigSourceFactory {
    private static IConfigSourceFactory instance;

    public static IConfigSourceFactory instance() {
        if (instance == null)
            instance = new ConfigSourceFactory();

        return instance;
    }

    /**
     * Create empty datasource. This is useful if the Trigger do not have config file.
     *
     * @param folder
     * @param fileName
     * @return the datasource which does nothing.
     */
    @Override
    public IConfigSource none(File folder, String fileName) {
        return new EmptyConfigSource();
    }

    /**
     * Create empty datasource. This is useful if the Trigger do not have config file.
     *
     * @param file
     * @return the datasource which does nothing.
     */
    @Override
    public IConfigSource none(File file) {
        return new EmptyConfigSource();
    }

    /**
     * Create a Gson based datasource.
     *
     * @param folder     the folder where json file is located
     * @param fileName   the filename ends with .json
     * @param validators validators to be used to serializable types
     * @return the datasource
     * @throws RuntimeException if folder is not directory.
     */
    @Override
    public IConfigSource gson(File folder, String fileName, ITypeValidator... validators) {
        if (!folder.exists())
            folder.mkdirs();

        if (!folder.isDirectory())
            throw new RuntimeException(folder + " must be a directory.");

        if (!fileName.endsWith(".json"))
            throw new RuntimeException(fileName + " does not ends with .json.");

        return new GsonConfigSource(new File(folder, fileName));
    }

    /**
     * Create a Gson based datasource.
     *
     * @param file       the json file
     * @param validators validators to be used to serializable types
     * @return the datasource
     * @throws RuntimeException file is not file
     */
    @Override
    public IConfigSource gson(File file, ITypeValidator... validators) {
        if (!file.isFile())
            throw new RuntimeException("it must be a file.");

        return new GsonConfigSource(file);
    }

    private static class EmptyConfigSource implements IConfigSource {
        @Override
        public boolean fileExists() {
            return true;
        }

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
