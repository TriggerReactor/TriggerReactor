package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.ThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.function.Predicate;

public class InventoryTrigger extends Trigger {
    @Inject
    IGameController gameController;
    @Inject
    ThrowableHandler throwableHandler;
    @Inject
    IScriptEngineProvider scriptEngineProvider;
    @Inject
    IWrapper wrapper;
    @Inject
    @Named("ItemStack")
    Class<?> itemClass;

    private int size;

    @AssistedInject
    InventoryTrigger(@Assisted TriggerInfo info, @Assisted String script) {
        super(info, script);

        size = readItems(info).length;
    }

    private IItemStack[] readItems(TriggerInfo info) {
        int size = info.getConfig()
                .get(SIZE, Integer.class)
                .filter(INVSIZE_PREDICATE)
                .filter(s -> s <= InventoryTrigger.MAXSIZE)
                .orElseThrow(() -> new InvalidTrgConfigurationException("Couldn't find or invalid Size",
                        info.getConfig()));

        IItemStack[] items = new IItemStack[size];

        if (info.getConfig().has(ITEMS)) {
            if (!info.getConfig().isSection(ITEMS)) {
                throw new InvalidTrgConfigurationException("Items should be a section", info.getConfig());
            }

            for (int i = 0; i < size; i++) {
                final int itemIndex = i;
                info.getConfig()
                        .get(ITEMS + "." + i, itemClass)
                        .ifPresent(item -> items[itemIndex] = wrapper.wrap(item));
            }
        }

        return items;
    }

    public InventoryTrigger(Trigger o) {
        super(o);
        ValidationUtil.assertTrue(o, v -> v instanceof InventoryTrigger);
        InventoryTrigger other = (InventoryTrigger) o;

        this.gameController = other.gameController;
        this.throwableHandler = other.throwableHandler;
    }

    @Override
    protected void start(Timings.Timing timing, Map<String, Object> scriptVars, Interpreter interpreter) {

        InterpreterLocalContext localContext = new InterpreterLocalContext(timing,
                gameController.createInterrupterForInv(cooldowns, InventoryTriggerManager.inventoryMap));
        localContext.putAllVars(scriptVars);
        localContext.setExtra(Interpreter.SCRIPT_ENGINE_KEY, scriptEngineProvider.getEngine());
        try {

        } catch (Exception ex) {
            throwableHandler.handleException(localContext, new Exception(
                    "Error occurred while processing Trigger [" + getInfo() + "]!", ex));
        }
    }

    /**
     * Get a snapshot of the items in this inventory.
     *
     * @return
     */
    public IItemStack[] getItems() {
        return readItems(info).clone();
    }

    public void setItems(IItemStack[] items) {
        ValidationUtil.notNull(items);
        ValidationUtil.assertTrue(items.length, INVSIZE_PREDICATE, "Invalid item size: " + items.length);

        for (int i = 0; i < items.length; i++) {
            IItemStack item = items[i];
            if (item == null)
                continue;

            info.getConfig().put(ITEMS + "." + i, item.get());
        }
        size = items.length;
    }

    public void setItem(IItemStack item, int index) {
        ValidationUtil.assertTrue(index, i -> i < 54 && i >= 0);

        info.getConfig().put(ITEMS + "." + index, item.get());

        notifyObservers();
    }

    public void setColumn(IItemStack item, int columnIndex) {
        int rows = size / 9;
        ValidationUtil.assertTrue(columnIndex, index -> index <= 8 && index >= 0);

        for (int row = 0; row < rows; row++) {
            info.getConfig().put(ITEMS + "." + (columnIndex + row * 9), item.get());
        }

        notifyObservers();
    }

    public void setRow(IItemStack item, int rowIndex) {
        int rows = size / 9;
        ValidationUtil.assertTrue(rowIndex, index -> index <= rows && index >= 0);

        for (int col = 0; col < 9; col++) {
            info.getConfig().put(ITEMS + "." + (rowIndex * 9 + col), item.get());
        }

        notifyObservers();
    }

    public void setTitle(String title) {
        info.getConfig().put(TITLE, title);

        notifyObservers();
    }

    public String getInventoryTitle() {
        return info.getConfig().get(TITLE, String.class).orElse(null);
    }

    public int size() {
        return size;
    }

    public static final Predicate<Integer> INVSIZE_PREDICATE = s -> s != 0 && s % 9 == 0;

    public static final int MAXSIZE = 6 * 9;
    public static final String ITEMS = "Items";
    public static final String SIZE = "Size";
    public static final String TITLE = "Title";

    public static IItemStack[] itemMapToArray(int size, Map<Integer, IItemStack> items) {
        checkSize(size);

        IItemStack[] arr = new IItemStack[size];
        for (Map.Entry<Integer, IItemStack> entry : items.entrySet()) {
            arr[entry.getKey()] = entry.getValue();
        }
        return arr;
    }

    private static void checkSize(int size) {
        if (size < 9 || size % 9 != 0)
            throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

        if (size > MAXSIZE)
            throw new IllegalArgumentException("Inventory Size cannot be larger than " + MAXSIZE);
    }
}
