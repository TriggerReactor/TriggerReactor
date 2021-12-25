package io.github.wysohn.triggerreactor.core.config.source;

import io.github.wysohn.triggerreactor.core.main.IPluginProcedure;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public interface IConfigSource extends IPluginProcedure {
    /**
     * Delete the config file.
     */
    void delete();

    /**
     * Check if valid .json file exists. Here, 'valid' means it's a valid json file.
     * For example, a valid json file must contain at least object ({}).
     *
     * @return
     */
    boolean fileExists();

    <T> Optional<T> get(String key);

    <T> Optional<T> get(String key, Class<T> asType);

    boolean has(String key);

    boolean isSection(String key);

    Set<String> keys();

    void put(String key, Object value);

    /**
     * Save all contents in memory to the permanent storage. This is usually
     * a blocking operation, so try not to call this from the main thread.
     */
    void saveAll();

    static String[] toPath(String key) {
        Queue<String> path = new LinkedList<>();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            if (key.charAt(i) == '.') {
                if (builder.length() > 0) {
                    path.add(builder.toString());
                    builder = new StringBuilder();
                }
                continue;
            }

            builder.append(key.charAt(i));
        }

        if (builder.length() > 0) {
            path.add(builder.toString());
        }

        return path.toArray(new String[0]);
    }
}
