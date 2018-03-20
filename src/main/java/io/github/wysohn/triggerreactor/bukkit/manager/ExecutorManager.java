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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

@SuppressWarnings("serial")
public class ExecutorManager extends AbstractExecutorManager implements BukkitScriptEngineInitializer{
    private File executorFolder;

    public ExecutorManager(TriggerReactor plugin) throws ScriptException, IOException {
        super(plugin);
        this.executorFolder = new File(plugin.getDataFolder(), "Executor");
        JarUtil.copyFolderFromJar("Executor", plugin.getDataFolder(), CopyOption.REPLACE_IF_EXIST);

        reload();
    }

    @Override
    public void reload(){
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().endsWith(".js");
            }
        };

        jsExecutors.clear();
        for(File file : executorFolder.listFiles(filter)){
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load executor "+file.getName());
                continue;
            }
        }

        //manually add CMDOP after reload is done
        this.jsExecutors.put("CMDOP", new Executor() {

            @Override
            protected Integer execute(boolean sync, Object e, Object... args) throws Exception {
                ///////////////////////////////
                Map<String, Object> variables = new HashMap<>();
                Map<String, Object> vars = ReflectionUtil.extractVariables(e);
                variables.putAll(vars);

                instance.extractCustomVariables(variables, e);
                ///////////////////////////////

                Object player = vars.get("player");
                if(player == null || !(player instanceof Player))
                    return null;

                boolean wasOp = false;
                try {
                    if (plugin.isServerThread()) {
                        wasOp = new IsOp((Player) player).call();
                    } else {
                        wasOp = plugin.callSyncMethod(new IsOp((Player) player)).get();
                    }

                    if(!wasOp) {
                        if (plugin.isServerThread()) {
                            new SetOp((Player) player, true).call();
                        } else {
                            plugin.callSyncMethod(new SetOp((Player) player, true)).get();
                        }
                    }

                    if(args.length > 0) {
                        if (plugin.isServerThread()) {
                            new DispatchCommand((Player) player, String.valueOf(args[0])).call();
                        } else {
                            plugin.callSyncMethod(new DispatchCommand((Player) player, String.valueOf(args[0]))).get();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if(!wasOp) {
                        if (plugin.isServerThread()) {
                            new SetOp((Player) player, false).call();
                        } else {
                            plugin.callSyncMethod(new SetOp((Player) player, false)).get();
                        }
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

    public static void main(String[] ar){
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        for(int i = 0; i < stack.size(); i++)
            System.out.println(i+". "+stack.get(i));
    }

    private class IsOp implements Callable<Boolean>{
        private final Player player;

        public IsOp(Player player) {
            super();
            this.player = player;
        }

        @Override
        public Boolean call() throws Exception {
            return player.isOp();
        }

    }

    private class DispatchCommand implements Callable<Void>{
        private final Player player;
        private final String cmd;

        public DispatchCommand(Player player, String cmd) {
            super();
            this.player = player;
            this.cmd = cmd;
        }

        @Override
        public Void call() throws Exception {
            Bukkit.dispatchCommand(player, cmd);
            return null;
        }

    }

    private class SetOp implements Callable<Void>{
        private final Player player;
        private final boolean op;
        public SetOp(Player player, boolean op) {
            super();
            this.player = player;
            this.op = op;
        }
        @Override
        public Void call() throws Exception {
            player.setOp(op);
            return null;
        }
    }
}
