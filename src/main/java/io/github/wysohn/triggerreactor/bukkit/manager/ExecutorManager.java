/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;

@SuppressWarnings("serial")
public class ExecutorManager extends AbstractExecutorManager{
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

        for(File file : executorFolder.listFiles(filter)){
            try {
                reloadExecutors(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load executor "+file.getName());
                continue;
            }
        }
    }



    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void extractCustomVariables(Map<String, Object> variables, Object e) {
        if(e instanceof InventoryInteractEvent){
            if(((InventoryInteractEvent) e).getWhoClicked() instanceof Player)
                variables.put("player", ((InventoryInteractEvent) e).getWhoClicked());
        } else if(e instanceof InventoryCloseEvent){
            if(((InventoryCloseEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryCloseEvent) e).getPlayer());
        } else if(e instanceof InventoryOpenEvent){
            if(((InventoryOpenEvent) e).getPlayer() instanceof Player)
                variables.put("player", ((InventoryOpenEvent) e).getPlayer());
        }
    }

    @Override
    protected void initScriptEngine() throws ScriptException {
        registerClass(Executor.class);
        registerClass(Bukkit.class);
        registerClass(Location.class);
        registerClass(ChatColor.class);
        registerClass(BukkitUtil.class);

        sem.put("plugin", this.plugin);

        sem.put("get", new Function<String, Object>(){
            @Override
            public Object apply(String t) {
                return plugin.getVariableManager().get(t);
            }
        });

        sem.put("put", new BiFunction<String, Object, Void>(){
            @Override
            public Void apply(String a, Object b) {
                if(!VariableManager.isValidName(a))
                    throw new RuntimeException("["+a+"] cannot be used as key");

                if(a != null && b == null){
                    plugin.getVariableManager().remove(a);
                } else{
                    try {
                        plugin.getVariableManager().put(a, b);
                    } catch (Exception e) {
                        throw new RuntimeException("Executor -- put("+a+","+b+")", e);
                    }
                }

                return null;
            }
        });

        sem.put("has", new Function<String, Boolean>(){
            @Override
            public Boolean apply(String t) {
                return plugin.getVariableManager().has(t);
            }
        });

        sem.put("Char", new Function<String, Character>(){
            @Override
            public Character apply(String t) {
                return t.charAt(0);
            }
        });
    }

    public static void main(String[] ar){
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        for(int i = 0; i < stack.size(); i++)
            System.out.println(i+". "+stack.get(i));
    }
}
