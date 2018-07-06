package io.github.wysohn.triggerreactor.sponge.tools;

import ninja.leaping.configurate.ConfigurationNode;

public class ConfigurationUtil {
    public static ConfigurationNode getNodeByKeyString(ConfigurationNode config, String key){
        String[] pathes = key.split("\\.");
        Object[] objs = new Object[pathes.length];
        for(int i = 0; i < objs.length; i++){
            objs[i] = pathes[i];
        }

        return config.getNode(objs);
    }
}
