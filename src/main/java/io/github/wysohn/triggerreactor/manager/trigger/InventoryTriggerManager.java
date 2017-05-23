package io.github.wysohn.triggerreactor.manager.trigger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;

import io.github.wysohn.triggerreactor.core.interpreter.Interpreter;
import io.github.wysohn.triggerreactor.core.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.parser.ParserException;
import io.github.wysohn.triggerreactor.main.TriggerReactor;
import io.github.wysohn.triggerreactor.manager.TriggerManager;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;

public class InventoryTriggerManager extends TriggerManager {
    private final Map<String, InventoryTrigger> invenTriggers = new ConcurrentHashMap<>();

    private final File folder;

    public InventoryTriggerManager(TriggerReactor plugin) {
        super(plugin);

        folder = new File(plugin.getDataFolder(), "InventoryTrigger");
        if(!folder.exists())
            folder.mkdirs();

        reload();
    }

    @Override
    public void reload() {
        FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".yml");
            }
        };

        for(File file : folder.listFiles(filter)){
            String fileName = file.getName();
            String triggerName = fileName.substring(0, fileName.indexOf('.'));

            File triggerFolder = new File(folder, triggerName);
            if(!triggerFolder.isDirectory()){
                plugin.getLogger().warning(triggerFolder+" is not a directory!");
                continue;
            }

            Utf8YamlConfiguration yaml = new Utf8YamlConfiguration();
            try {
                yaml.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
            }

            if(!yaml.contains("Size")){
                plugin.getLogger().warning("Could not find Size: for inventory trigger "+triggerName);
                continue;
            }
            int size = yaml.getInt("Size");
            if(size % 9 != 0){
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
                plugin.getLogger().warning("Size: must be multiple of 9!");
                continue;
            }

            Map<Integer, String> scriptMap = new HashMap<>();
            File slotFolder = new File(file, triggerName);
            if(!slotFolder.exists()){
                slotFolder.mkdirs();
                continue;
            }

            for(int i = 0; i < size; i++){
                File slotTrigger = new File(slotFolder, String.valueOf(i));
                if(!slotTrigger.exists())
                    continue;

                if(!slotTrigger.isFile())
                    continue;

                try(FileInputStream fis = new FileInputStream(slotTrigger);
                        InputStreamReader isr = new InputStreamReader(fis, "UTF-8")){
                    StringBuilder builder = new StringBuilder();
                    int read = -1;
                    while((read = isr.read()) != -1){
                        builder.append((char) read);
                    }
                    String script = builder.toString();

                    scriptMap.put(i, script);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                invenTriggers.put(triggerName, new InventoryTrigger(scriptMap, size));
            } catch (InvalidSlotException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Could not load inventory trigger "+triggerName);
            }
        }
    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    private class InventoryTrigger extends Trigger{
        final static int SLOTSPERPAGE = 5*9;

        final InventorySlot[] slots;
        final int pageCount;

        public InventoryTrigger(Map<Integer, String> scriptMap, int size) throws InvalidSlotException{
            super(null);

            if(size < 9 || size % 9 != 0)
                throw new IllegalArgumentException("Inventory Trigger size should be multiple of 9!");

            slots = new InventorySlot[size];

            for(Map.Entry<Integer, String> entry : scriptMap.entrySet()){
                try {
                    slots[entry.getKey()] = new InventorySlot(entry.getValue());
                } catch (IOException | LexerException | ParserException e) {
                    throw new InvalidSlotException(entry.getKey(), e);
                }
            }

            pageCount = (size / SLOTSPERPAGE) + 1;
        }

        //intercept and pass interpretation to slots
        @Override
        protected void startInterpretation(Event e, Map<String, Object> scriptVars, Interpreter interpreter) {
            InventoryClickEvent ice = (InventoryClickEvent) e;

            int rawSlot = ice.getRawSlot();
            if(rawSlot >= 0 && rawSlot < slots.length){
                InventorySlot slot;
                if (pageCount > 1) {
                    slot = slots[rawSlot];
                    if(slot != null)
                        slot.activate(e, scriptVars);
                }else{
                    int page = extractPageFromTitle(ice.getInventory().getTitle()) - 1;

                    slot = slots[rawSlot + page * SLOTSPERPAGE];
                    if(slot != null)
                        slot.activate(e, scriptVars);
                }
            }

        }

        @Override
        public Trigger clone() {
            // TODO Auto-generated method stub
            return null;
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////

        private class InventorySlot extends Trigger{

            public InventorySlot(String script) throws IOException, LexerException, ParserException {
                super(script);

                init();
            }

            @Override
            public void activate(Event e, Map<String, Object> scriptVars) {
                super.activate(e, scriptVars);
            }

            @Override
            public Trigger clone() {
                return null;
            }

        }
    }

    @SuppressWarnings("serial")
    static class InvalidSlotException extends Exception{
        public InvalidSlotException(int slot, Throwable cause) {
            super("Slot "+slot+" could not be initialized!", cause);
        }
    }

    private static final String SEPARATOR = ":";
    /**
     *
     * @param page starting from 1
     * @param title
     * @return combined title
     */
    public static String getTitleWithPage(int page, String title){
        return page+SEPARATOR+title;
    }
    /**
     *
     * @param title
     * @return starting from 1
     */
    public static int extractPageFromTitle(String title){
        return Integer.parseInt(title.split(SEPARATOR, 2)[0]);
    }
}
