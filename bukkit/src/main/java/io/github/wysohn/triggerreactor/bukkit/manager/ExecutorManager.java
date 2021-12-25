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

import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@Singleton
public class ExecutorManager extends AbstractExecutorManager {
    private static final String JAR_FOLDER_LOCATION = "Executor";
    @Inject
    TaskSupervisor task;
    @Inject
    @Named("DataFolder")
    File dataFolder;
    @Inject
    Logger logger;
    private File executorFolder;

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {
        JarUtil.copyFolderFromJar(JAR_FOLDER_LOCATION, dataFolder, CopyOption.REPLACE_IF_EXIST);
        this.executorFolder = new File(dataFolder, "Executor");

        onReload();
    }

    @Override
    public void onReload() {
        FileFilter filter = pathname -> pathname.isDirectory() || pathname.getName().endsWith(".js");

        jsExecutors.clear();
        for (File file : executorFolder.listFiles(filter)) {
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                logger.warning("Could not load executor " + file.getName());
                continue;
            }
        }

        //manually add CMDOP after reload is done
        this.jsExecutors.put("CMDOP", new Executor() {

            @Override
            public Integer execute(Timings.Timing timing,
                                   Map<String, Object> variables,
                                   Object e,
                                   Object... args) throws Exception {
                Object player = variables.get("player");
                if (!(player instanceof Player)) return null;

                DispatchCommandAsOP call = new DispatchCommandAsOP((Player) player, String.valueOf(args[0]));
                if (task.isServerThread()) {
                    call.call();
                } else {
                    try {
                        task.submitSync(call).get();
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

        @Override
        public Void call() throws Exception {
            wasOp = player.isOp();

            try {
                player.setOp(true);

//                if (plugin.getCmdManager() instanceof CommandTriggerManager) {
//                    CommandTriggerManager manager = (CommandTriggerManager) plugin.getCmdManager();
//                    PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, cmd);
//                    manager.onCommand(event);
//                    if (!event.isCancelled())
//                        Bukkit.dispatchCommand(player, cmd);
//                } else {
//                    Bukkit.dispatchCommand(player, cmd);
//                }

                Bukkit.dispatchCommand(player, cmd); // now we register CommandTrigger to commandMap
            } catch (Exception e) {

            } finally {
                deOpIfWasNotOp();
            }
            return null;
        }

        private void deOpIfWasNotOp() {
            if (!wasOp) player.setOp(false);
        }

    }
}
