package io.github.wysohn.triggerreactor.core.config.source;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public interface IConfigSource {
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

    /**
     * Check if valid .json file exists. Here, 'valid' means it's a valid json file.
     * For example, a valid json file must contain at least object ({}).
     *
     * @return
     */
    boolean fileExists();

    <T> Optional<T> get(String key, Class<T> asType);

    <T> Optional<T> get(String key);

    void put(String key, Object value);

    boolean has(String key);

    Set<String> keys();

    boolean isSection(String key);

    void reload();

    /**
     * Save all contents in memory to the permanent storage. This is usually
     * a blocking operation, so try not to call this from the main thread.
     */
    void saveAll();

    /**
     * Prepare to shutdown the config source. This doesn't really do anything
     * unless the underlying child class has multi-thread implementation to
     * reduce the I/O delays, etc. If the child class is using multi-thread
     * implementation, this method must be called before the server shutdown,
     * so the shutdown procedure can wait for the non-main threads to gracefully
     * shutdown without losing the necessary information.
     */
    void disable();

    /**
     * Delete the config file.
     */
    void delete();
}
