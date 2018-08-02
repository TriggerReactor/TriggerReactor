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
package io.github.wysohn.triggerreactor.sponge.manager;

import java.io.File;
import java.io.IOException;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

public class VariableManager extends AbstractVariableManager{
    private File varFile;

    private ConfigurationLoader<ConfigurationNode> varFileConfigLoader;
    private ConfigurationNode varFileConfig;

    private final GlobalVariableAdapter adapter;

    public VariableManager(TriggerReactor plugin) throws IOException {
        super(plugin);

        varFile = new File(plugin.getDataFolder(), "var.yml");
        if(!varFile.exists())
            varFile.createNewFile();

        varFileConfigLoader = YAMLConfigurationLoader.builder().setPath(varFile.toPath()).build();

        reload();

        adapter = new VariableAdapter();

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
        try {
            varFileConfig = varFileConfigLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void saveAll(){
        try {
            varFileConfigLoader.save(varFileConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GlobalVariableAdapter getGlobalVariableAdapter(){
        return adapter;
    }

    private ConfigurationNode getNodeByKeyString(String key){
        String[] pathes = key.split("\\.");
        Object[] objs = new Object[pathes.length];
        for(int i = 0; i < objs.length; i++){
            objs[i] = pathes[i];
        }

        return varFileConfig.getNode(objs);
    }

    @Override
    public Object get(String key){
        ConfigurationNode targetNode = getNodeByKeyString(key);
        return targetNode.getValue();
    }

    @Override
    public void put(String key, Object value){
        ConfigurationNode targetNode = getNodeByKeyString(key);
        targetNode.setValue(value);
    }

    @Override
    public boolean has(String key){
        ConfigurationNode targetNode = getNodeByKeyString(key);
        return !targetNode.isVirtual();
    }

    @Override
    public void remove(String key){
        ConfigurationNode targetNode = getNodeByKeyString(key);
        ConfigurationNode parent = targetNode.getParent();
        parent.removeChild(targetNode.getKey());
    }

    @SuppressWarnings("serial")
    public class VariableAdapter extends GlobalVariableAdapter{

        @Override
        public Object get(Object key) {
            Object value = null;

            //try global if none found in local
            if(value == null && key instanceof String){
                String keyStr = (String) key;
                if(has(keyStr)){
                    ConfigurationNode targetNode = getNodeByKeyString(keyStr);
                    value = targetNode.getValue();
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
                ConfigurationNode targetNode = getNodeByKeyString(keyStr);
                result = !targetNode.isVirtual();
            }

            return result;
        }

        @Override
        public Object put(String key, Object value) {
            ConfigurationNode targetNode = getNodeByKeyString(key);
            Object before = targetNode.getValue();
            targetNode.setValue(value);
            return before;
        }
    }
}
