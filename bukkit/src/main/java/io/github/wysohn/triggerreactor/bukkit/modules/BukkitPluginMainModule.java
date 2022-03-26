/*
 *     Copyright (C) 2021 wysohn and contributors
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.bukkit.modules;

import dagger.Module;
import dagger.Provides;
import io.github.wysohn.triggerreactor.bukkit.main.*;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitEventRegistryManager;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.BukkitGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.components.PluginMainComponent;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.IResourceProvider;
import io.github.wysohn.triggerreactor.core.manager.ResourceManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ICommandMapHandler;
import io.github.wysohn.triggerreactor.core.manager.trigger.custom.IEventRegistry;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.inject.Named;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.logging.Logger;

@Module(subcomponents = PluginMainComponent.class)
public class BukkitPluginMainModule {
    private final Plugin plugin;
    private final IWrapper wrapper;
    private final SelfReference reference;
    private final ScriptEngineManager scriptEngineManager;

    public BukkitPluginMainModule(Plugin plugin,
                                  IWrapper wrapper,
                                  SelfReference reference,
                                  ScriptEngineManager scriptEngineManager) {
        this.plugin = plugin;
        this.wrapper = wrapper;
        this.reference = reference;
        this.scriptEngineManager = scriptEngineManager;
    }

    @Provides
    @Named("ItemStack")
    static Class<?> provideItemStackClass(){
        return ItemStack.class;
    }

    @Provides
    static Server provideServer() {
        return Bukkit.getServer();
    }

    @Provides
    static PluginManager bindPluginManager(Server server) {
        return server.getPluginManager();
    }

    @Provides
    public Plugin providePlugin() {
        return plugin;
    }

    @Provides
    @Named("PluginInstance")
    public Object providePluginInstance() {
        return plugin;
    }

    @Provides
    @Named("DataFolder")
    public File provideDataFolder() {
        return plugin.getDataFolder();
    }

    @Provides
    public Logger providerLogger(){
        return plugin.getLogger();
    }

    @Provides
    public IWrapper provideWrapper() {
        return wrapper;
    }

    @Provides
    public SelfReference provideSelfReference(){
        return reference;
    }

    @Provides
    public IGameController provideGameController(BukkitGameController controller) {
        return controller;
    }

    @Provides
    public IInventoryModifier provideInventoryModifier(BukkitInventoryModifier modifier) {
        return modifier;
    }

    @Provides
    public IGUIOpenHelper provideGUIOpenHelper(BukkitGUIOpenHelper helper) {
        return helper;
    }

    @Provides
    public IPluginLifecycleController provideLifecycleController(BukkitPluginLifecycle controller) {
        return controller;
    }

    @Provides
    public TaskSupervisor provideTaskSupervisor(BukkitTaskSupervisor supervisor) {
        return supervisor;
    }

    @Provides
    public ScriptEngineManager provideScriptEngineManager(){
        return scriptEngineManager;
    }

    @Provides
    public ICommandMapHandler provideCommandMapHandler(BukkitCommandMapHandler handler) {
        return handler;
    }

    @Provides
    public IEventRegistry provideEventRegistry(BukkitEventRegistryManager manager) {
        return manager;
    }

    @Provides
    public IResourceProvider provideResourceProvider(ResourceManager manager) {
        return manager;
    }
}
