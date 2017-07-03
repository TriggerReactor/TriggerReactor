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

import javax.script.ScriptException;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;

@SuppressWarnings("serial")
public class ExecutorManager extends AbstractExecutorManager{
    private File executorFolder;

    public ExecutorManager(TriggerReactor plugin) throws ScriptException, IOException {
        super(plugin);
        this.executorFolder = new File(plugin.getDataFolder(), "Executor");
        JarUtil.copyFolderFromJar("Executor", plugin.getDataFolder(), CopyOption.COPY_IF_NOT_EXIST);

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
                reloadExecutors(new Stack<String>(), file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load executor "+file.getName());
                continue;
            }
        }
    }

    private void reloadExecutors(Stack<String> name, File file, FileFilter filter) throws ScriptException, IOException{
        if(file.isDirectory()){
            name.push(file.getName());
            for(File f : file.listFiles(filter)){
                reloadExecutors(name, f, filter);
            }
            name.pop();
        }else{
            StringBuilder builder = new StringBuilder();
            for(int i = name.size() - 1; i >= 0; i--){
                builder.append(name.get(i)+":");
            }
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            builder.append(fileName);

            if(jsExecutors.containsKey(builder.toString())){
                plugin.getLogger().warning(builder.toString()+" already registered! Duplicating executors?");
            }else{
                JSExecutor exec = new JSExecutor(fileName, file);
                jsExecutors.put(builder.toString(), exec);
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

    public static void main(String[] ar){
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        for(int i = 0; i < stack.size(); i++)
            System.out.println(i+". "+stack.get(i));
    }
}
