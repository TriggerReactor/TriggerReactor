package io.github.wysohn.triggerreactor.core.components;

import dagger.BindsInstance;
import dagger.Component;
import io.github.wysohn.triggerreactor.core.manager.IInventoryModifier;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;

import javax.inject.Named;
import javax.inject.Singleton;

@Component
@Singleton
public interface InventoryUtilComponent {
    IInventoryModifier inventoryModifier();

    @Named("ItemStack")
    Class<?> itemStack();

    IGUIOpenHelper guiOpenHelper();

    @Component.Builder
    interface Builder {
        InventoryUtilComponent build();

        @BindsInstance
        Builder inventoryModifier(IInventoryModifier inventoryModifier);

        @BindsInstance
        Builder itemStackClass(@Named("ItemStack") Class<?> itemStackClass);

        @BindsInstance
        Builder guiOpenHelper(IGUIOpenHelper guiOpenHelper);
    }
}
