package io.github.wysohn.triggerreactor.core.config.source;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigSourceFactories {
    @Inject
    @Named("DefaultConfigType")
    String defaultFactoryKey;
    @Inject
    Map<String, IConfigSourceFactory> factories = new HashMap<>();

    @Inject
    public ConfigSourceFactories() {
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
        return create(defaultFactoryKey, folder, fileName);
    }

    /**
     * @param type     type of the config source.
     * @param folder   the folder where config file will reside
     * @param fileName name of the file <b>without</b> any dots. The underlying
     *                 factory will append the extension as needed.
     * @return the new config source responsible for the given folder and fileName
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

        return factories.get(type).create(type, folder, fileName);
    }

}
