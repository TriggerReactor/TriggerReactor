package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.manager.config.IConfigSource;
import io.github.wysohn.triggerreactor.tools.FileUtil;

import java.io.File;

public abstract class TriggerInfo {
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

    public File getSourceCodeFile() {
        return sourceCodeFile;
    }

    public IConfigSource getConfig() {
        return config;
    }

    public String getTriggerName() {
        return triggerName;
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
        FileUtil.delete(sourceCodeFile);
        config.delete();
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
        if (!file.isFile())
            return null;

        if (file.getName().indexOf('.') == -1)
            return file.getName();

        return file.getName().substring(0, file.getName().indexOf('.'));
    }

    @Override
    public String toString() {
        return triggerName;
    }

    /**
     * Trigger info with default behavior: it is a valid TriggerInfo if the `sourceCodeFile` passes
     * {@link #isTriggerFile(File)}
     */
    public static TriggerInfo defaultInfo(File sourceCodeFile, IConfigSource config) {
        return new TriggerInfo(sourceCodeFile, config) {
            @Override
            public boolean isValid() {
                return isTriggerFile(getSourceCodeFile());
            }
        };
    }
}
