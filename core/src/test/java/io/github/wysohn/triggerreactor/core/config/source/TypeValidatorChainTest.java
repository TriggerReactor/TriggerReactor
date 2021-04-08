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

import io.github.wysohn.triggerreactor.core.config.validation.DefaultValidator;
import io.github.wysohn.triggerreactor.core.config.validation.SimpleChunkLocationValidator;
import io.github.wysohn.triggerreactor.core.config.validation.SimpleLocationValidator;
import io.github.wysohn.triggerreactor.core.config.validation.UUIDValidator;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleChunkLocation;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeValidatorChainTest {
    @Test
    public void testBuilder() {
        ITypeValidator validator = new TypeValidatorChain.Builder()
                .addChain(new DefaultValidator())
                .addChain(new UUIDValidator())
                .addChain(new SimpleLocationValidator())
                .addChain(new SimpleChunkLocationValidator())
                .build();


        assertTrue(validator.isSerializable(1));
        assertTrue(validator.isSerializable(2.0));
        assertTrue(validator.isSerializable(true));
        assertTrue(validator.isSerializable("String"));
        assertTrue(validator.isSerializable(Collections.emptyList()));
        assertTrue(validator.isSerializable(Collections.emptySet()));
        assertTrue(validator.isSerializable(new Object[]{8, 7.9, false, "abc"}));
        assertTrue(validator.isSerializable(UUID.randomUUID()));
        assertTrue(validator.isSerializable(new SimpleLocation("world", 3, 4, 5)));
        assertTrue(validator.isSerializable(new SimpleChunkLocation("world", 22, 33)));

        assertFalse(validator.isSerializable(this));

        // map should be handled as section separately, so it's not serializable
        assertFalse(validator.isSerializable(new HashMap<>()));
    }
}