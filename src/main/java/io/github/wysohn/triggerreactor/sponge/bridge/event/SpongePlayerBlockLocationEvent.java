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
import io.github.wysohn.triggerreactor.sponge.bridge.player.SpongePlayer;
import io.github.wysohn.triggerreactor.sponge.manager.event.PlayerBlockLocationEvent;

public class SpongePlayerBlockLocationEvent implements IPlayerBlockLocationEvent {
    private final PlayerBlockLocationEvent event;

    public SpongePlayerBlockLocationEvent(PlayerBlockLocationEvent event) {
        super();
        this.event = event;
    }

    @Override
    public IPlayer getIPlayer() {
        return new SpongePlayer(event.getTargetEntity());
    }

    @Override
    public <T> T get() {
        return (T) event;
    }

    @Override
    public SimpleLocation getFrom() {
        return event.getFrom();
    }

    @Override
    public SimpleLocation getTo() {
        return event.getTo();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        event.setCancelled(b);
    }

}
