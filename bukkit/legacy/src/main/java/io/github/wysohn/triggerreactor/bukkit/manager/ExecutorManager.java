/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.manager.trigger.CommandTriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;

@SuppressWarnings("serial")
public class ExecutorManager extends AbstractExecutorManager implements BukkitScriptEngineInitializer {
    private static final String JAR_FOLDER_LOCATION = "Executor";

    private File executorFolder;

    public ExecutorManager(TriggerReactor plugin) throws ScriptException, IOException {
        super(plugin);
        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, plugin.getDataFolder(), CopyOption.REPLACE_IF_EXIST);

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

                DispatchCommandAsOP call = new DispatchCommandAsOP((Player) player, String.valueOf(args[0]));
                if (plugin.isServerThread()) {
                    call.call();
                } else {
                    try {
                        plugin.callSyncMethod(call).get();
                    } catch (Exception ex) {
                        //to double check
                        call.deOpIfWasNotOp();
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
        BukkitScriptEngineInitializer.super.initScriptEngine(sem);
    }

    public static void main(String[] ar) {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        for (int i = 0; i < stack.size(); i++)
            System.out.println(i + ". " + stack.get(i));
    }

    private class DispatchCommandAsOP implements Callable<Void> {
        private final Player player;
        private final String cmd;

        private boolean wasOp;

        public DispatchCommandAsOP(Player player, String cmd) {
            super();
            this.player = player;
            this.cmd = cmd;
        }

        private void deOpIfWasNotOp() {
            if (!wasOp)
                player.setOp(false);
        }

        @Override
        public Void call() throws Exception {
            wasOp = player.isOp();

            try {
                player.setOp(true);

                if (plugin.getCmdManager() instanceof CommandTriggerManager) {
                    CommandTriggerManager manager = (CommandTriggerManager) plugin.getCmdManager();
                    PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, cmd);
                    manager.onCommand(event);
                    if (!event.isCancelled())
                        Bukkit.dispatchCommand(player, cmd);
                } else {
                    Bukkit.dispatchCommand(player, cmd);
                }
            } catch (Exception e) {

            } finally {
                deOpIfWasNotOp();
            }
            return null;
        }

    }
}
