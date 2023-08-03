package io.github.wysohn.triggerreactor.core.config.source;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ConfigSourceFactory {
    private static ConfigSourceFactory instance;
    static final String DEFAULT_FACTORY = "gson";
    private final Map<String, IConfigSourceFactory> factories = new HashMap<>();

    @Inject
    private Injector injector;

    @Inject
    private ConfigSourceFactory() {
        factories.put("none", (type, folder, fileName) -> new EmptyConfigSource());
        factories.put("gson", (type, folder, fileName) -> {
            IConfigSource source = new GsonConfigSource(new File(folder, fileName + ".json"));
            source.reload();
            return source;
        });
    }

    /**
     * Register new config source factory.
     *
     * @param type    the name to be used to represent this factory
     * @param factory the factory instance
     * @return null if registered; factory instance of the name already in use
     */
    public IConfigSourceFactory registerFactory(String type, IConfigSourceFactory factory) {
        return factories.put(type, factory);
    }

    /**
     * Create a ConfigSource using the default factory (gson).
     *
     * @param folder   the folder where config file will reside
     * @param fileName name of the file <b>without</b> any dots. The underlying
     *                 factory will append the extension as needed.
     * @return the new config source responsible for the given folder and fileName
     */
    public IConfigSource create(File folder, String fileName) {
        return create(DEFAULT_FACTORY, folder, fileName);
    }

    /**
     * @param type     type of the config source.
     * @param folder   the folder where config file will reside
     * @param fileName name of the file <b>without</b> any dots. The underlying
     *                 factory will append the extension as needed.
     * @return the new/existing config source responsible for the given folder and fileName
     */
    public IConfigSource create(String type, File folder, String fileName) {
        if (!folder.exists())
            folder.mkdirs();

        if (!folder.isDirectory())
            throw new RuntimeException(folder + " must be a directory.");

        if (fileName.lastIndexOf('.') != -1)
            throw new RuntimeException("fileName must not include dot(.).");

        if (!factories.containsKey(type))
            throw new RuntimeException(type + " is not a registered type.");

        IConfigSource configSource = factories.get(type).create(type, folder, fileName);
        injector.injectMembers(configSource);
        return configSource;
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
