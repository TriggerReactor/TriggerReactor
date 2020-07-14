/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
package io.github.wysohn.triggerreactor.core.manager.trigger.share.api;

import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Abstract representation of API support. The child classes must have at least one constructor with one argument,
 * {@link io.github.wysohn.triggerreactor.core.main.TriggerReactorCore}, to make it work properly.
 *
 * @author wysohn
 */
public abstract class AbstractAPISupport {

    protected final TriggerReactorCore plugin;

    public AbstractAPISupport(TriggerReactorCore plugin) {
        super();
        this.plugin = plugin;
    }

    /**
     * Initialize this API. It may throw APISupportException if the plugin is not found.
     *
     * @throws APISupportException throw this exception when the supporting API is not loaded or not found.
     */
    public abstract void init() throws APISupportException;

    /**
     * Try to add a new instance of type 'clazz' to 'varName' if the 'varName' doesn't exist already.
     *
     * @param sharedVars the map to save variables
     * @param varName    the name of the variable
     * @param clazz      the class that will be instanciated.
     */
    public static void addSharedVar(Map<String, AbstractAPISupport> sharedVars, String varName, Class<? extends AbstractAPISupport> clazz) {
        if (!sharedVars.containsKey(varName)) {
            Constructor<?> con = null;
            try {
                con = clazz.getConstructor(TriggerReactorCore.class);
            } catch (NoSuchMethodException | SecurityException e1) {
                e1.printStackTrace();
            }

            boolean initSuccess = true;
            AbstractAPISupport api = null;
            try {
                api = (AbstractAPISupport) con.newInstance(TriggerReactorCore.getInstance());
                api.init();
            } catch (APISupportException e) {
                initSuccess = false;
            } catch (Exception e) {
                initSuccess = false;
                e.printStackTrace();
            } finally {
                if (api != null && initSuccess)
                    sharedVars.put(varName, api);
            }
        }
    }
}