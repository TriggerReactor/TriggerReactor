package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.config.IMigratable;
import io.github.wysohn.triggerreactor.core.config.IMigrationHelper;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public abstract class TriggerInfo implements IMigratable {
    private final File sourceCodeFile;
    private final IConfigSource config;
    private final String triggerName;

    public TriggerInfo(File sourceCodeFile, IConfigSource config) {
        this.sourceCodeFile = sourceCodeFile;
        this.config = config;
        this.triggerName = extractName(sourceCodeFile);
    }

    public TriggerInfo(File sourceCodeFile, IConfigSource config, String triggerName) {
        this.sourceCodeFile = sourceCodeFile;
        this.config = config;
        this.triggerName = triggerName;
    }

    @Override
    public boolean isMigrationNeeded() {
        if(sourceCodeFile == null)
            return false;

        File folder = sourceCodeFile.getParentFile();
        File oldFile = new File(folder, triggerName + ".yml");
        File newFile = new File(folder, triggerName + ".json");

        // new file not exist and old file exist
        return !newFile.exists() && oldFile.exists();
    }

    @Override
    public void migrate(IMigrationHelper migrationHelper) {
        Optional.ofNullable(config)
                .ifPresent(migrationHelper::migrate);
        reloadConfig();
    }

    public File getSourceCodeFile() {
        return sourceCodeFile;
    }

    public void reloadConfig() {
        Optional.ofNullable(config)
                .ifPresent(IConfigSource::reload);
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void put(TriggerConfigKey key, Object value) {
        ValidationUtil.notNull(config);

        config.put(key.getKey(), value);
    }

    public void put(TriggerConfigKey key, int index, Object value) {
        ValidationUtil.notNull(config);

        config.put(key.getKey(index), value);
    }

    public <T> Optional<T> get(TriggerConfigKey key, Class<T> clazz) {
        Optional<T> old = Optional.ofNullable(key.getOldKey())
                .flatMap(oldKey -> config.get(oldKey, clazz));
        if(old.isPresent())
            return old;

        return Optional.ofNullable(key.getKey())
                .flatMap(newKey -> config.get(newKey, clazz));
    }

    public <T> Optional<T> get(TriggerConfigKey key, int index, Class<T> clazz) {
        Optional<T> old = Optional.ofNullable(key.getOldKey(index))
                .flatMap(oldKey -> config.get(oldKey, clazz));
        if(old.isPresent())
            return old;

        return Optional.of(key.getKey(index))
                .flatMap(newKey -> config.get(newKey, clazz));
    }

    public boolean has(TriggerConfigKey key) {
        Optional<Boolean> old = Optional.ofNullable(key.getOldKey())
                .map(config::has);
        Optional<Boolean> current = Optional.ofNullable(key.getKey())
                .map(config::has);
        return old.orElse(false) || current.orElse(false);
    }

    public boolean isSection(TriggerConfigKey key) {
        Optional<Boolean> old = Optional.ofNullable(key.getOldKey())
                .map(config::isSection);
        Optional<Boolean> current = Optional.ofNullable(key.getKey())
                .map(config::isSection);
        return old.orElse(false) || current.orElse(false);
    }

    public boolean isSync() {
        return get(TriggerConfigKey.KEY_SYNC, Boolean.class).orElse(false);
    }

    public void setSync(boolean sync) {
        put(TriggerConfigKey.KEY_SYNC, sync);
    }

    /**
     * Check whether this TriggerInfo is valid or not so that it may can be used to instantiate the Trigger instance.
     *
     * @return true if valid; false if not (will be skipped for loading)
     */
    public abstract boolean isValid();

    /**
     * Default behavior is delete one file associated with the trigger. Override this method to change this behavior.
     */
    public void delete() {
        Optional.ofNullable(sourceCodeFile)
                .ifPresent(File::delete);
        Optional.ofNullable(config)
                .ifPresent(IConfigSource::delete);
    }

    /**
     * Check if the file has valid extension (.trg) or no extension.
     * No extension file is produced in old versions so have to be renamed accordingly.
     *
     * @param file the source code file to check
     * @return true if has valid extension; false if not file or extension is not valid
     */
    public static boolean isTriggerFile(File file) {
        if (!file.isFile())
            return false;

        String name = file.getName();

        //either ends with .trg or no extension
        return name.endsWith(".trg") || name.indexOf('.') == -1;
    }

    /**
     * extract file name without the extension
     *
     * @param file
     * @return the filename. null if the file is not file
     */
    public static String extractName(File file) {
        if (file.isDirectory())
            return null;

        if (file.getName().indexOf('.') == -1)
            return file.getName();

        return file.getName().substring(0, file.getName().indexOf('.'));
    }

    @Override
    public String toString() {
        return "TriggerInfo{" +
                "triggerName=" + triggerName +
                ", config='" + config + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TriggerInfo that = (TriggerInfo) o;

        return Objects.equals(triggerName, that.triggerName);
    }

    @Override
    public int hashCode() {
        return triggerName != null ? triggerName.hashCode() : 0;
    }

    /**
     * Trigger info with default behavior: it is a valid TriggerInfo if the `sourceCodeFile` passes
     * {@link #isTriggerFile(File)}
     */
    public static TriggerInfo defaultInfo(File sourceCodeFile, IConfigSource config) {
        return new TriggerInfo(sourceCodeFile, config) {
            @Override
            public boolean isValid() {
                return isTriggerFile(sourceCodeFile);
            }
        };
    }
}
