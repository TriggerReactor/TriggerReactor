package io.github.wysohn.triggerreactor.manager;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.event.PlayerPermissionCheckEvent;
import io.github.wysohn.triggerreactor.tools.FileUtil;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class PermissionManager extends Manager implements Listener{
    private boolean inject = true;

    public PermissionManager(TriggerReactor plugin) {
        super(plugin);

        try {
            String configStr = FileUtil.readFromStream(plugin.getResource("config.yml"));
            FileUtil.writeToFile(new File(plugin.getDataFolder(), "config.yml"), configStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            plugin.getConfig().addDefault("PermissionManager.Intercept", true);
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveConfig();
        }
    }

    @Override
    public void reload() {
        plugin.reloadConfig();

        inject = plugin.getConfig().getBoolean("PermissionManager.Intercept", false);
    }

    @Override
    public void saveAll() {
        plugin.saveConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        if(!inject)
            return;

        injectPermissible(e.getPlayer());
    }

    private boolean failed = false;
    private void injectPermissible(Player player) {
        //So it doesn't spam
        if(failed)
            return;

        Class<?> clazz = player.getClass();
        while(clazz.getSuperclass() != null && !clazz.getSimpleName().equals("CraftHumanEntity")){
            clazz = clazz.getSuperclass();
        }

        try {
            ReflectionUtil.setFinalField(clazz, player, "perm", new PermissibleInterceptor(player));
        } catch (NoSuchFieldException | IllegalArgumentException e) {
            e.printStackTrace();
            failed = true;
            plugin.getLogger().severe("Could not inject permission interceptor.");
            plugin.getLogger().severe("PlayerPermissionCheckEvent will no longer fired.");
        }
    }

    private static class PermissibleInterceptor extends PermissibleBase{
        private final Player player;

        public PermissibleInterceptor(Player opable) {
            super(opable);
            this.player = opable;
        }

        @Override
        public boolean hasPermission(String inName) {
            PlayerPermissionCheckEvent ppce = new PlayerPermissionCheckEvent(player, inName);
            Bukkit.getPluginManager().callEvent(ppce);
            if(ppce.isCancelled()){
                return ppce.isAllowed();
            }

            return super.hasPermission(inName);
        }

        @Override
        public boolean hasPermission(Permission perm) {
            PlayerPermissionCheckEvent ppce = new PlayerPermissionCheckEvent(player, perm.getName());
            Bukkit.getPluginManager().callEvent(ppce);
            if(ppce.isCancelled()){
                return ppce.isAllowed();
            }

            return super.hasPermission(perm);
        }


    }
}
