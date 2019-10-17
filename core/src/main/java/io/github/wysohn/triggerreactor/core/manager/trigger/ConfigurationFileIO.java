package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.File;

public interface ConfigurationFileIO {
    /**
     * Get data from yml file. It may not needed depends on the trigger type.
     * For CustomTrigger for example, you can get data from the yml file with
     * this method.
     *
     * @param file file to read data from
     * @param key  yml key
     * @param def  default value if key does not exists
     * @return the value
     */
    <T> T getData(File file, String key, T def) throws Exception;

    /**
     * Set data into yml file. It may not work depends on the trigger type.
     *
     * @param file  file to write data
     * @param key
     * @param value
     * @return
     */
    void setData(File file, String key, Object value) throws Exception;
}
