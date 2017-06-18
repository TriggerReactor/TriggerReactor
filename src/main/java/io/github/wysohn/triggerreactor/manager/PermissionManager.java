package io.github.wysohn.triggerreactor.manager;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.event.PlayerPermissionCheckEvent;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class PermissionManager extends Manager implements Listener{
    private boolean inject = true;

    public PermissionManager(TriggerReactor plugin) {
        super(plugin);

        plugin.getConfig().addDefault("PermissionManager.Intercept", true);
        plugin.saveConfig();

        reload();
    }

    @Override
    public void reload() {
        plugin.reloadConfig();

        inject = plugin.getConfig().getBoolean("PermissionManager.Intercept", false);

        for(Player p : Bukkit.getOnlinePlayers()){
            PermissibleBase original = getPermissible(p);
            if(inject){
                //inject if not already injected
                if(!(original instanceof PermissibleInterceptor)){
                    injectPermissible(p, new PermissibleInterceptor(p));
                }
            }else{
                //change back to original if already injected
                if(original instanceof PermissibleInterceptor){
                    injectPermissible(p, ((PermissibleInterceptor) original).getOriginal());
                }
            }
        }
    }

    @Override
    public void saveAll() {
        plugin.saveConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e){
        if(!inject)
            return;

        injectPermissible(e.getPlayer(), new PermissibleInterceptor(e.getPlayer()));
    }

    private boolean failed = false;
    private void injectPermissible(Player player, PermissibleBase newPermissible) {
        //So it doesn't spam
        if(failed)
            return;

        Class<?> clazz = player.getClass();
        while(clazz.getSuperclass() != null && !clazz.getSimpleName().equals("CraftHumanEntity")){
            clazz = clazz.getSuperclass();
        }

        try {
            PermissibleBase original = (PermissibleBase) ReflectionUtil.getField(clazz, player, "perm");
            List<PermissionAttachment> attachments = (List<PermissionAttachment>) ReflectionUtil.getField(original, "attachments");

            ReflectionUtil.setFinalField(PermissibleBase.class, newPermissible, "attachments", attachments);

            ReflectionUtil.setFinalField(clazz, player, "perm", newPermissible);
            newPermissible.recalculatePermissions();

            if(newPermissible instanceof PermissibleInterceptor){
                ((PermissibleInterceptor) newPermissible).setOriginal(original);
            }
        } catch (NoSuchFieldException | IllegalArgumentException e) {
            e.printStackTrace();
            failed = true;
            plugin.getLogger().severe("Could not inject permission interceptor.");
            plugin.getLogger().severe("PlayerPermissionCheckEvent will no longer fired.");
        }
    }

    private PermissibleBase getPermissible(Player player){
        Class<?> clazz = player.getClass();
        while(clazz.getSuperclass() != null && !clazz.getSimpleName().equals("CraftHumanEntity")){
            clazz = clazz.getSuperclass();
        }

        try {
            PermissibleBase original = (PermissibleBase) ReflectionUtil.getField(clazz, player, "perm");

            return original;
        } catch (NoSuchFieldException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class PermissibleInterceptor extends PermissibleBase{
        private final Player player;
        private PermissibleBase original;

        public PermissibleInterceptor(Player opable) {
            super(opable);
            this.player = opable;
        }

        public PermissibleBase getOriginal() {
            return original;
        }

        public void setOriginal(PermissibleBase original) {
            this.original = original;
        }

        @Override
        public boolean hasPermission(String inName) {
            PlayerPermissionCheckEvent ppce = new PlayerPermissionCheckEvent(player, inName);
            Bukkit.getPluginManager().callEvent(ppce);
            if(ppce.isCancelled()){
                return ppce.isAllowed();
            }

            if(original != null){
                return original.hasPermission(inName);
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

            if(original != null){
                return original.hasPermission(perm);
            }

            return super.hasPermission(perm);
        }


    }
}
