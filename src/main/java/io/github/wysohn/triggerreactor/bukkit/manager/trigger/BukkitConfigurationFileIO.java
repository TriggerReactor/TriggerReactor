package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;

import io.github.wysohn.triggerreactor.core.manager.trigger.ConfigurationFileIO;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;

public interface BukkitConfigurationFileIO extends ConfigurationFileIO {
    @Override
    default <T> T getData(File file, String key, T def) throws IOException {
        Utf8YamlConfiguration conf = new Utf8YamlConfiguration();
        try {
            conf.load(file);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return def;
        }

        return (T) conf.get(key);
    }

    @Override
    default void setData(File file, String key, Object value) throws IOException {
        Utf8YamlConfiguration conf = new Utf8YamlConfiguration();
        try {
            conf.load(file);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        conf.set(key, value);
        conf.save(file);
    }
}
