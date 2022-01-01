package io.github.wysohn.triggerreactor.core.manager.trigger.inventory;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.ThrowableHandler;
import io.github.wysohn.triggerreactor.core.manager.IScriptEngineProvider;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;

public class InventoryTrigger extends Trigger {
    final IItemStack[] items;
    @Inject
    IGameController gameController;
    @Inject
    ThrowableHandler throwableHandler;
    @Inject
    IScriptEngineProvider scriptEngineProvider;

    @AssistedInject
    InventoryTrigger(@Assisted TriggerInfo info, @Assisted String script, @Assisted IItemStack[] items) {
        super(info, script);
        this.items = items;
    }

    public InventoryTrigger(Trigger o) {
        super(o);
        ValidationUtil.assertTrue(o, v -> v instanceof InventoryTrigger);
        InventoryTrigger other = (InventoryTrigger) o;

        this.items = Arrays.copyOf(other.items, other.items.length);
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
            throwableHandler.handleException(localContext,
                    new Exception("Error occurred while processing Trigger [" + getInfo() + "]!", ex));
        }
    }

    /**
     * Get a snapshot of the items in this inventory.
     * @return
     */
    public IItemStack[] getItems() {
        return Arrays.copyOf(items, items.length);
    }

    /**
     * Get current inventory size
     * @return
     */
    public int size(){
        return items.length;
    }

    /**
     * Number of rows. Same as {@link #size() / 9}
     * @return
     */
    public int rows(){
        return items.length / 9;
    }

    public void setItem(IItemStack item, int index){
        ValidationUtil.assertTrue(index, i -> i < items.length && i >= 0);

        items[index] = item;

        notifyObservers();
    }

    public void setColumn(IItemStack item, int columnIndex){
        int rows = items.length / 9;
        ValidationUtil.assertTrue(columnIndex, index -> index <= 8 && index >= 0);

        for (int row = 0; row < rows; row++) {
            items[columnIndex + row * 9] = item;
        }

        notifyObservers();
    }

    public void setRow(IItemStack item, int rowIndex){
        int rows = items.length / 9;
        ValidationUtil.assertTrue(rowIndex, index -> index <= rows && index >= 0);

        for (int col = 0; col < 9; col++) {
            items[rowIndex * 9 + col] = item;
        }

        notifyObservers();
    }

    public void setTitle(String title) {
        info.getConfig().put(InventoryTriggerManager.TITLE, title);
        notifyObservers();
    }

    public static final int MAXSIZE = 6 * 9;

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
