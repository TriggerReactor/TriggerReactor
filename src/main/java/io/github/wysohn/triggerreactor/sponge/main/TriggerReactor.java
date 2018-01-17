package io.github.wysohn.triggerreactor.sponge.main;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import io.github.wysohn.triggerreactor.bukkit.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.event.IEvent;
import io.github.wysohn.triggerreactor.core.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.AbstractAreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPermissionManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlayerLocationManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractScriptEditManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractAreaTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCommandTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractCustomTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractInventoryTriggerManager.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractLocationBasedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractLocationBasedTriggerManager.ClickTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractLocationBasedTriggerManager.WalkTrigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractNamedTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractRepeatingTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager.Trigger;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter.ProcessInterrupter;

@Plugin(id = "triggerreactor")
public class TriggerReactor extends io.github.wysohn.triggerreactor.core.main.TriggerReactor{
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    @Override
    public AbstractExecutorManager getExecutorManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractPlaceholderManager getPlaceholderManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractVariableManager getVariableManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractScriptEditManager getScriptEditManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractPlayerLocationManager getLocationManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractPermissionManager getPermissionManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractAreaSelectionManager getSelectionManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractLocationBasedTriggerManager<ClickTrigger> getClickManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractLocationBasedTriggerManager<WalkTrigger> getWalkManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractCommandTriggerManager getCmdManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractInventoryTriggerManager getInvManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractAreaTriggerManager getAreaManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractCustomTriggerManager getCustomManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractRepeatingTriggerManager getRepeatManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractNamedTriggerManager getNamedTriggerManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Listener
    public void onEnable(GameStartingServerEvent e){

    }

    @Listener
    public void onDisable(GameStoppingServerEvent e){

    }

    @Override
    protected boolean removeLore(IItemStack iS, int index) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean setLore(IItemStack iS, int index, String lore) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void addItemLore(IItemStack iS, String lore) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setItemTitle(IItemStack iS, String title) {
        // TODO Auto-generated method stub

    }

    @Override
    protected IPlayer getPlayer(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Object createEmptyPlayerEvent(ICommandSender sender) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void sendCommandDesc(ICommandSender sender, String command, String desc) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void sendDetails(ICommandSender sender, String detail) {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getPluginDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void showGlowStones(ICommandSender sender, Set<Entry<SimpleLocation, Trigger>> set) {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerEvents(Manager manager) {
        // TODO Auto-generated method stub

    }

    @Override
    public File getDataFolder() {
        return this.privateConfigDir.toFile();
    }

    @Override
    public Logger getLogger() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void disablePlugin() {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> T getMain() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isConfigSet(String key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setConfig(String key, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getConfig(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getConfig(String key, T def) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveConfig() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reloadConfig() {
        // TODO Auto-generated method stub

    }

    @Override
    public void runTask(Runnable runnable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAsynchronously(Manager manager) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleException(Object e, Throwable ex) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleException(ICommandSender sender, Throwable ex) {
        // TODO Auto-generated method stub

    }

    @Override
    public ProcessInterrupter createInterrupter(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProcessInterrupter createInterrupterForInv(Object e, Interpreter interpreter, Map<UUID, Long> cooldowns,
            Map<IInventory, InventoryTrigger> inventoryMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUID extractUUIDFromContext(Object e) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> call) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void callEvent(IEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isServerThread() {
        boolean result = false;

        synchronized(this){
            result = Sponge.getServer().isMainThread();
        }

        return result;
    }
}
