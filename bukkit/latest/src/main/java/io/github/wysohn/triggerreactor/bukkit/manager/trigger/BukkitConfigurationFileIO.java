package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.bukkit.tools.Utf8YamlConfiguration;
import io.github.wysohn.triggerreactor.core.manager.trigger.ConfigurationFileIO;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

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

        T result = (T) conf.get(key);
        if (result == null)
            result = def;
        return result;
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
