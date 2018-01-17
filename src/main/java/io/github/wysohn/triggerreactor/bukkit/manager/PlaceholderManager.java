package io.github.wysohn.triggerreactor.bukkit.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
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
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.tools.JarUtil;
import io.github.wysohn.triggerreactor.tools.JarUtil.CopyOption;

public class PlaceholderManager extends AbstractPlaceholderManager {
    private File placeholderFolder;

    public PlaceholderManager(TriggerReactor plugin) throws ScriptException, IOException {
        super(plugin);
        this.placeholderFolder = new File(plugin.getDataFolder(), "Placeholder");
        JarUtil.copyFolderFromJar("Placeholder", plugin.getDataFolder(), CopyOption.REPLACE_IF_EXIST);

        reload();
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
                        throw new RuntimeException("Placeholder -- put("+a+","+b+")", e);
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
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".js");
            }
        };

        jsPlaceholders.clear();
        for(File file : placeholderFolder.listFiles(filter)){
            try {
                reloadPlaceholders(file, filter);
            } catch (ScriptException | IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load placeholder "+file.getName());
                continue;
            }
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

}
