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
package io.github.wysohn.triggerreactor.core.manager.config.serialize;

import copy.com.google.gson.JsonDeserializationContext;
import copy.com.google.gson.JsonElement;
import copy.com.google.gson.JsonParseException;
import copy.com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDSerializer implements Serializer<UUID> {

    @Override
    public JsonElement serialize(UUID arg0, Type arg1, JsonSerializationContext arg2) {
        return arg2.serialize(arg0.toString());
    }

    @Override
    public UUID deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        return UUID.fromString(arg2.deserialize(arg0, String.class));
    }

}
