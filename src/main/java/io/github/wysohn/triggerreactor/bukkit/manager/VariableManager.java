/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.bukkit.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import io.github.wysohn.triggerreactor.bukkit.main.TriggerReactor;
import io.github.wysohn.triggerreactor.misc.Utf8YamlConfiguration;

public class VariableManager extends Manager{
    private File varFile;
    private FileConfiguration varFileConfig;
    private GlobalVariableAdapter adapter;

    public VariableManager(TriggerReactor plugin) throws IOException, InvalidConfigurationException {
        super(plugin);

        varFile = new File(plugin.getDataFolder(), "var.yml");
        if(!varFile.exists())
            varFile.createNewFile();

        reload();

        adapter = new GlobalVariableAdapter();

        new VariableAutoSaveThread().start();
    }

    private class VariableAutoSaveThread extends Thread{
        VariableAutoSaveThread(){
            setPriority(MIN_PRIORITY);
            this.setName("TriggerReactor Variable Saving Thread");
        }

        @Override
        public void run(){
            while(!Thread.interrupted() && plugin.isEnabled()){
                saveAll();

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void reload(){
        varFileConfig = new Utf8YamlConfiguration();
        try {
            varFileConfig.load(varFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void saveAll(){
        try {
            varFileConfig.save(varFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GlobalVariableAdapter getGlobalVariableAdapter(){
        return adapter;
    }

    public Object get(String key){
        return varFileConfig.get(key);
    }

    public void put(String key, Object value){
        varFileConfig.set(key, value);
    }

    public boolean has(String key){
        return varFileConfig.contains(key);
    }

    public void remove(String key){
        varFileConfig.set(key, null);
    }

    @SuppressWarnings("serial")
    public class GlobalVariableAdapter extends HashMap<String, Object>{
        private GlobalVariableAdapter(){

        }
        @Override
        public Object get(Object key) {
            Object value = null;

            //try global if none found in local
            if(value == null && key instanceof String){
                String keyStr = (String) key;
                if(varFileConfig.contains(keyStr)){
                    value = varFileConfig.get(keyStr);
                }
            }

            return value;
        }

        @Override
        public boolean containsKey(Object key) {
            boolean result = false;

            //check global if none found in local
            if(!result && key instanceof String){
                String keyStr = (String) key;
                result = varFileConfig.contains(keyStr);
            }

            return result;
        }

        @Override
        public Object put(String key, Object value) {
            Object before = varFileConfig.get(key);
            varFileConfig.set(key, value);
            return before;
        }
    }

    private static final Pattern pattern = Pattern.compile(
            "# Match a valid Windows filename (unspecified file system).          \n" +
            "^                                # Anchor to start of string.        \n" +
            "(?!                              # Assert filename is not: CON, PRN, \n" +
            "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
            "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
            "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
            "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
            "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
            "  $                              # and end of string                 \n" +
            ")                                # End negative lookahead assertion. \n" +
            "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
            "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
            "$                                # Anchor to end of string.            ",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);

    public static boolean isValidName(String str){
        return pattern.matcher(str).matches();
    }
}
