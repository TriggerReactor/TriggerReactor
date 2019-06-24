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

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.manager.AbstractVariableManager;
import io.github.wysohn.triggerreactor.sponge.tools.ConfigurationUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class VariableManager extends AbstractVariableManager {
    private File varFile;

    private ConfigurationLoader<CommentedConfigurationNode> varFileConfigLoader;
    private ConfigurationNode varFileConfig;

    private Boolean saving = false;

    public VariableManager(TriggerReactor plugin) throws IOException {
        super(plugin);

        varFile = new File(plugin.getDataFolder(), "var.yml");
        if (!varFile.exists())
            varFile.createNewFile();

        varFileConfigLoader = HoconConfigurationLoader.builder().setPath(varFile.toPath()).build();

        reload();
    }

    @Override
    public void reload() {
        plugin.getLogger().info("Waiting for previous saving tasks...");
        synchronized (saving) {
            try {
                plugin.getLogger().info("Done! now reloading global variables...");
                varFileConfig = varFileConfigLoader.load();
                plugin.getLogger().info("Global variables were loaded from " + varFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveAll() {
        if (saving)
            return;

        saving = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (saving) {
                    try {
                        varFileConfigLoader.save(varFileConfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                        plugin.getLogger().severe("Something went wrong while saving global variable!");
                    } finally {
                        saving = false;
                    }
                }
            }
        }).start();
    }

    @Override
    public Object get(String key) {
        ConfigurationNode parentNode = ConfigurationUtil.getNodeByKeyString(varFileConfig, key);
        if (parentNode.isVirtual())
            return null;

        ConfigurationNode typeNode = ConfigurationUtil.getNodeByKeyString(parentNode, "type");
        ConfigurationNode valueNode = ConfigurationUtil.getNodeByKeyString(parentNode, "value");

        if (typeNode.isVirtual())
            throw new RuntimeException("Can't find type for " + key);

        if (valueNode.isVirtual())
            throw new RuntimeException("Can't find value for " + key);

        String typeName = typeNode.getString();
        if (typeName.equals("String")) {
            return valueNode.getString();
        } else if (typeName.equals("Integer")) {
            return valueNode.getInt();
        } else if (typeName.equals("Decimal")) {
            return valueNode.getDouble();
        } else {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("No such class " + typeNode.getString());
            }
            if (!DataSerializable.class.isAssignableFrom(clazz))
                throw new RuntimeException(typeNode.getString() + " is not DataSerializable");

            DataBuilder builder = Sponge.getDataManager().getBuilder((Class<? extends DataSerializable>) clazz).orElse(null);
            if (builder == null)
                throw new RuntimeException(typeNode.getString() + " has no appropriate DataBuilder");

            DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;
            DataView container = translator.translate(valueNode);
            return builder.build(container).orElse(null);
        }
    }

    @Override
    public void put(String key, Object value) {
        if (value == null) {
            ConfigurationNode node = ConfigurationUtil.getNodeByKeyString(varFileConfig, key);
            node.getParent().removeChild(node);
        } else {
            ConfigurationNode typeNode = ConfigurationUtil.getNodeByKeyString(varFileConfig, key + ".type");
            ConfigurationNode valueNode = ConfigurationUtil.getNodeByKeyString(varFileConfig, key + ".value");

            if (value instanceof String) {
                typeNode.setValue("String");
                valueNode.setValue(value);
            } else if (value instanceof Number) {
                switch (value.getClass().getSimpleName()) {
                    case "AtomicInteger":
                    case "BigInteger":
                    case "Byte":
                    case "Integer":
                    case "Short":
                    case "Long":
                    case "AtomicLong":
                        typeNode.setValue("Integer");
                        valueNode.setValue(((Number) value).intValue());
                        break;
                    case "BigDecimal":
                    case "Double":
                    case "Float":
                        typeNode.setValue("Decimal");
                        valueNode.setValue(((Number) value).doubleValue());
                        break;
                    default:
                        throw new RuntimeException(value + " is not DataSerializable");
                }
            } else if (value instanceof DataSerializable) {
                DataSerializable ds = (DataSerializable) value;
                DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;

                if (value instanceof ItemStack) {
                    typeNode.setValue(ItemStack.class.getName());
                    valueNode.setValue(translator.translate(ds.toContainer()));
                } else {
                    typeNode.setValue(value.getClass().getName());
                    valueNode.setValue(translator.translate(ds.toContainer()));
                }
            } else {
                throw new RuntimeException(value + " is not DataSerializable");
            }
        }
    }

    @Override
    public boolean has(String key) {
        ConfigurationNode targetNode = ConfigurationUtil.getNodeByKeyString(varFileConfig, key);
        return !targetNode.isVirtual();
    }

    @Override
    public void remove(String key) {
        ConfigurationNode targetNode = ConfigurationUtil.getNodeByKeyString(varFileConfig, key);
        ConfigurationNode parent = targetNode.getParent();
        parent.removeChild(targetNode.getKey());
    }
}
