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

import io.github.wysohn.triggerreactor.core.main.IPluginProcedure;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;

/**
 * Abstract representation of API support. The child classes must have at least one constructor with one argument,
 * {@link TriggerReactorMain}, to make it work properly.
 *
 * @author wysohn
 */
public abstract class AbstractAPISupport implements IPluginProcedure {
    protected final Object targetPluginInstance;

    public AbstractAPISupport(Object targetPluginInstance) {
        this.targetPluginInstance = targetPluginInstance;
    }

    /**
     * Get variable name to be used to access this api support from the
     * trigger scripts. For example, we can access Vault support using
     * 'vault' variable.
     *
     * @return
     */
    public abstract String getVariableName();
}