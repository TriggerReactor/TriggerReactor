/*
 *     Copyright (C) 2021 wysohn and contributors
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.config.source;

import java.util.Optional;
import java.util.Set;

public class EmptyConfigSource implements IConfigSource {
    @Override
    public boolean fileExists() {
        return true;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> asType) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> get(String key) {
        return Optional.empty();
    }

    @Override
    public void put(String key, Object value) {

    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public Set<String> keys() {
        return null;
    }

    @Override
    public boolean isSection(String key) {
        return false;
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onReload() {

    }

    @Override
    public void saveAll() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void delete() {

    }
}
