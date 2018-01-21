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
package io.github.wysohn.triggerreactor.sponge.bridge.event;

import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

public class SpongePlayerBlockLocationEvent implements IPlayerBlockLocationEvent {

    @Override
    public IPlayer getIPlayer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T get() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimpleLocation getFrom() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimpleLocation getTo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCancelled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
        // TODO Auto-generated method stub

    }

}
