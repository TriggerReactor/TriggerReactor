package io.github.wysohn.triggerreactor.core.manager.trigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

import io.github.wysohn.triggerreactor.bridge.IInventory;
import io.github.wysohn.triggerreactor.bridge.IItemStack;
import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.TriggerManager;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.event.IInventoryCloseEvent;
import io.github.wysohn.triggerreactor.core.script.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

public abstract class AbstractInventoryTriggerManager extends TriggerManager {

    protected final Map<String, InventoryTrigger> invenTriggers = new ConcurrentHashMap<>();
    protected static final Map<IInventory, InventoryTrigger> inventoryMap = new ConcurrentHashMap<>();
    protected final Map<IInventory, Map<String, Object>> inventorySharedVars = new ConcurrentHashMap<>();

    public static class InventoryTrigger extends Trigger{
        public static final int MAXSIZE = 6*9;

        final IItemStack[] items;

        private InventoryTrigger(String name, String script, IItemStack[] items) throws IOException, LexerException, ParserException {
            super(name, script);
            this.items = items;

            init();
        }

        public InventoryTrigger(int size, String name, Map<Integer, IItemStack> items, String script) throws IOException, LexerException, ParserException{
            super(name, script);
            if(size < 9 || size % 9 != 0)
                throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

            if(size > MAXSIZE)
                throw new IllegalArgumentException("Inventory Size cannot be larger than "+MAXSIZE);

            this.items = new IItemStack[size];

            for(Map.Entry<Integer, IItemStack> entry : items.entrySet()){
                this.items[entry.getKey()] = entry.getValue();
            }

            init();
        }

        @Override
        protected void start(Object e, Map<String, Object> scriptVars, Interpreter interpreter, boolean sync) {
            try {
                interpreter.startWithContextAndInterrupter(e,
                        TriggerReactor.getInstance().createInterrupterForInv(e, interpreter, cooldowns, inventoryMap));
            } catch (Exception ex) {
                ex.printStackTrace();

                TriggerReactor.getInstance().handleException(e, ex);
            }
        }

        @Override
        public Trigger clone() {
            try {
                return new InventoryTrigger(triggerName, script, items);
            } catch (IOException | LexerException | ParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        public IItemStack[] getItems() {
            return items;
        }
    }

    /**
    *
    * @param player
    * @param name
    * @return the opened Inventory's reference; null if no Inventory Trigger found
    */
   public Inventory openGUI(IPlayer player, String name){
       InventoryTrigger trigger = invenTriggers.get(name);
       if(trigger == null)
           return null;

       Inventory inventory = Bukkit.createInventory(null, trigger.getItems().length,
               ChatColor.translateAlternateColorCodes('&', name));
       inventoryMap.put(new BukkitInventory(inventory), trigger);

       Map<String, Object> varMap = new HashMap<>();
       varMap.put("inventory", inventory);
       inventorySharedVars.put(new BukkitInventory(inventory), varMap);

       fillInventory(trigger, trigger.getItems().length, new BukkitInventory(inventory));

       player.openInventory(new BukkitInventory(inventory));

       return inventory;
   }

    /**
     *
     * @param name
     * @return null if not exists
     */
    public InventoryTrigger getTriggerForName(String name) {
        return invenTriggers.get(name);
    }

    /**
     *
     * @param name
     *            this can contain color code &, but you should specify exact
     *            name for the title.
     * @return true on success; false if already exist
     * @throws ParserException
     *             See {@link Trigger#init()}
     * @throws LexerException
     *             See {@link Trigger#init()}
     * @throws IOException
     *             See {@link Trigger#init()}
     */
    public boolean createTrigger(int size, String name, String script)
            throws IOException, LexerException, ParserException {
        if (invenTriggers.containsKey(name))
            return false;

        invenTriggers.put(name, new InventoryTrigger(size, name, new HashMap<>(), script));

        return true;
    }

    /**
     *
     * @param name
     * @return true on success; false if not exists
     */
    public boolean deleteTrigger(String name) {
        if(!invenTriggers.containsKey(name))
            return false;

        deleteInfo(invenTriggers.remove(name));

        return true;
    }

    protected abstract void deleteInfo(InventoryTrigger trigger);

    /**
     *
     * @param trigger
     * @param size mutiple of 9; must be less than or equalt to 54 (exclusive)
     * @param inventory
     */
    protected abstract void fillInventory(InventoryTrigger trigger, int size, IInventory inventory);
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onInventoryClose(IInventoryCloseEvent e, IPlayer player, IInventory inventory) {
        if (!inventoryMap.containsKey(inventory))
            return;
        InventoryTrigger trigger = inventoryMap.get(inventory);

        Map<String, Object> varMap = inventorySharedVars.get(inventory);
        varMap.put("player", player.get());
        varMap.put("trigger", "close");

        trigger.activate(e, varMap);

        inventoryMap.remove(inventory);
        inventorySharedVars.remove(inventory);
    }

    public AbstractInventoryTriggerManager(TriggerReactor plugin) {
        super(plugin);
    }

}