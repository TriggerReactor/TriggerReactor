package io.github.wysohn.triggerreactor.bukkit.manager;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerPermissionCheckEvent;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractPermissionManager;
import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class PermissionManager extends AbstractPermissionManager implements Listener{
    private boolean inject = true;

    public PermissionManager(TriggerReactor plugin) {
        super(plugin);

        if(!plugin.isConfigSet("PermissionManager.Intercept"))
            plugin.setConfig("PermissionManager.Intercept", true);
        plugin.saveConfig();

        reload();
    }

    @Override
    public void reload() {
        plugin.reloadConfig();

        inject = plugin.getConfig("PermissionManager.Intercept", false);

        for(Player p : BukkitUtil.getOnlinePlayers()){
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
            List<PermissionAttachment> attachments = (List<PermissionAttachment>) ReflectionUtil.getField(PermissibleBase.class, original, "attachments");

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
            Callable<Boolean> call = new Callable<Boolean>(){

                @Override
                public Boolean call() throws Exception {
                    PlayerPermissionCheckEvent ppce = new PlayerPermissionCheckEvent(player, inName);
                    Bukkit.getPluginManager().callEvent(ppce);
                    if(ppce.isCancelled()){
                        return ppce.isAllowed();
                    }
                    return false;
                }

            };

            if(Bukkit.isPrimaryThread()){
                try {
                    if(call.call())
                        return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Future<Boolean> future = TriggerReactor.getInstance().callSyncMethod(call);
                try {
                    return future.get(1, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    throw new RuntimeException("Took too long to process PlayerPermissionCheckEvent.");
                }
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

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
            if(original != null)
                return original.addAttachment(plugin, name, value);

            return super.addAttachment(plugin, name, value);
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin) {
            if(original != null)
                return original.addAttachment(plugin);

            return super.addAttachment(plugin);
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
            if(original != null)
                return original.addAttachment(plugin, name, value, ticks);

            return super.addAttachment(plugin, name, value, ticks);
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
            if(original != null)
                return original.addAttachment(plugin, ticks);

            return super.addAttachment(plugin, ticks);
        }

        @Override
        public boolean isOp() {
            if(original != null)
                return original.isOp();

            return super.isOp();
        }

        @Override
        public void setOp(boolean value) {
            if(original != null){
                original.setOp(value);
                return;
            }

            super.setOp(value);
        }

        @Override
        public boolean isPermissionSet(String name) {
            if(original != null)
                return original.isPermissionSet(name);

            return super.isPermissionSet(name);
        }

        @Override
        public boolean isPermissionSet(Permission perm) {
            if(original != null)
                original.isPermissionSet(perm);

            return super.isPermissionSet(perm);
        }

        @Override
        public void removeAttachment(PermissionAttachment attachment) {
            if(original != null){
                original.removeAttachment(attachment);
                return;
            }

            super.removeAttachment(attachment);
        }

        @Override
        public void clearPermissions() {
            if(original != null){
                original.clearPermissions();
                return;
            }

            super.clearPermissions();
        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            if(original != null)
                return original.getEffectivePermissions();

            return super.getEffectivePermissions();
        }

        @Override
        public void recalculatePermissions() {
            if(original != null){
                original.recalculatePermissions();
                return;
            }

            super.recalculatePermissions();
        }


    }
}
