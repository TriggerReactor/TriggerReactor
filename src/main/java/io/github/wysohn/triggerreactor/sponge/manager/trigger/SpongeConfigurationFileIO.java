package io.github.wysohn.triggerreactor.sponge.manager.trigger;

import java.io.File;
import java.io.IOException;

import io.github.wysohn.triggerreactor.core.manager.trigger.ConfigurationFileIO;
import io.github.wysohn.triggerreactor.sponge.tools.ConfigurationUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public interface SpongeConfigurationFileIO extends ConfigurationFileIO {
    @Override
    default <T> T getData(File file, String key, T def) throws IOException {
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file.toPath()).build();
        ConfigurationNode conf = loader.load();

        T result = (T) ConfigurationUtil.getNodeByKeyString(conf, key).getValue();
        if(result == null)
            result = def;
        return result;
    }

    @Override
    default void setData(File file, String key, Object value) throws IOException {
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file.toPath()).build();
        ConfigurationNode conf = loader.load();

        ConfigurationUtil.getNodeByKeyString(conf, key).setValue(value);
        loader.save(conf);
    }
}
