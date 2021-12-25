package io.github.wysohn.triggerreactor.sponge.manager;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class PlaceholderManager extends AbstractPlaceholderManager implements SpongeScriptEngineInitializer {
    private File placeholderFolder;

    public PlaceholderManager(TriggerReactorCore plugin) throws ScriptException, IOException {
        super(plugin);
        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION,
                                  plugin.getDataFolder(),
                                  CopyOption.REPLACE_IF_EXIST,
                                  (original) -> {
                                      return original.substring(0, original.indexOf("!" + JarUtil.JAR_SEPARATOR))
                                              .replace("." + JarUtil.JAR_SEPARATOR, "");
                                  });

        this.placeholderFolder = new File(plugin.getDataFolder(), "Placeholder");

        reload();
    }

    @Override
    public void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        super.initScriptEngine(sem);
        SpongeScriptEngineInitializer.super.initScriptEngine(sem);
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".js");
            }
        };

        jsPlaceholders.clear();
        for (File file : placeholderFolder.listFiles(filter)) {
            try {
                reloadPlaceholders(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load placeholder " + file.getName());
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }
    private static final String JAR_FOLDER_LOCATION = "Placeholder";

}
