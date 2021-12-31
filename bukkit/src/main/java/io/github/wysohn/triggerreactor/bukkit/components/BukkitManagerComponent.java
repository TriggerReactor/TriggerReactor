package io.github.wysohn.triggerreactor.bukkit.components;

import dagger.Component;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitManagerModule;
import io.github.wysohn.triggerreactor.bukkit.scope.BukkitManager;
import io.github.wysohn.triggerreactor.core.components.ManagerComponent;
import io.github.wysohn.triggerreactor.core.manager.*;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;

import java.util.Set;

@Component(modules = BukkitManagerModule.class,
           dependencies = {ManagerComponent.class,
                           BukkitGameControllerComponent.class,
                           BukkitPluginLifecycleComponent.class})
@BukkitManager
public interface BukkitManagerComponent {
    Set<Manager> managers();

    ExternalAPIManager externalAPIManager();

    AreaSelectionManager areaSelectionManager();

    ExecutorManager executorManager();

    InventoryEditManager inventoryEditManager();

    AbstractPermissionManager permissionManager();

    PlaceholderManager placeholderManager();

    PlayerLocationManager playerLocationManager();

    ScriptEditManager scriptEditManager();
}
