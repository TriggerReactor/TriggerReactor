package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import io.github.wysohn.triggerreactor.core.config.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class NamedTriggerInfo extends TriggerInfo {
    private final File folder;

    public NamedTriggerInfo(File folder, File sourceCodeFile, IConfigSource config, String triggerName) {
        super(sourceCodeFile, config, triggerName);
        this.folder = folder;
    }

    public NamedTriggerInfo(File folder, File sourceCodeFile, IConfigSource config) {
        super(sourceCodeFile, config);
        this.folder = folder;
    }

    @Override
    public boolean isValid() {
        return isTriggerFile(getSourceCodeFile());
    }

    @Override
    public String getTriggerName() {
        return toConventionalName(folder, getSourceCodeFile());
    }

    /**
     * get name which includes the path. For example, if the triggerName of this NamedTrigger is `abc` and it's in
     * the folder `some/folder/`, then the result will be `some:folder:abc`
     *
     * @return the named to be used for NamedTrigger convention
     */
    public static String toConventionalName(File folder, File sourceCodeFile) {
        Path folderPath = folder.toPath();
        Path filePath = sourceCodeFile.toPath();
        Path relative = folderPath.relativize(filePath);

        return relative.toString()
                .replaceAll(Pattern.quote(File.separator), ":")
                .replace(".trg", "");
    }
}
