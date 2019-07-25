package io.github.wysohn.triggerreactor.sponge.manager;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.sponge.tools.TemporarilyPrivilegedPlayer;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

public class ExecutorManager extends AbstractExecutorManager implements SpongeScriptEngineInitializer {
    private static final String JAR_FOLDER_LOCATION = "Executor";

    private File executorFolder;

    public ExecutorManager(TriggerReactor plugin) throws ScriptException, IOException {
        super(plugin);
        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, plugin.getDataFolder(), CopyOption.REPLACE_IF_EXIST, (original) -> {
            return original.substring(0, original.indexOf("!" + JarUtil.JAR_SEPARATOR)).replace("." + JarUtil.JAR_SEPARATOR, "");
        });

        this.executorFolder = new File(plugin.getDataFolder(), "Executor");

        reload();
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().endsWith(".js");
            }
        };

        jsExecutors.clear();
        for (File file : executorFolder.listFiles(filter)) {
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load executor " + file.getName());
                continue;
            }
        }

        //manually add CMDOP after reload is done
        this.jsExecutors.put("CMDOP", new Executor() {

            @Override
            protected Integer execute(boolean sync, Map<String, Object> variables, Object e, Object... args) throws Exception {
                Object player = variables.get("player");
                if (player == null || !(player instanceof Player))
                    return null;

                if (args.length > 0) {
                    if (plugin.isServerThread()) {
                        new DispatchCommand((Player) player, String.valueOf(args[0])).call();
                    } else {
                        plugin.callSyncMethod(new DispatchCommand((Player) player, String.valueOf(args[0]))).get();
                    }
                }

                return null;
            }

        });
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initScriptEngine(ScriptEngineManager sem) throws ScriptException {
        super.initScriptEngine(sem);
        SpongeScriptEngineInitializer.super.initScriptEngine(sem);
    }

    private class DispatchCommand implements Callable<Void> {
        private final Player player;
        private final String cmd;

        public DispatchCommand(Player player, String cmd) {
            super();
            this.player = player;
            this.cmd = cmd;
        }

        @Override
        public Void call() throws Exception {
            // sponge has some convenient stuff here :)
            // this bypass the permission check
            Sponge.getCommandManager().process(new TemporarilyPrivilegedPlayer(player), cmd);
            return null;
        }

    }

}
