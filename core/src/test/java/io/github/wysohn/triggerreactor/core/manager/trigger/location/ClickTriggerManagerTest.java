/*
 * Copyright (C) 2023. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.manager.trigger.location;

import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ClickTriggerManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    ClickTriggerManager manager;


    @Before
    public void setUp() throws Exception {


        manager = Guice.createInjector().getInstance(ClickTriggerManager.class);
    }

    @Test
    public void getTriggerTypeName() {
        assertNotNull(manager.getTriggerTypeName());
    }

    @Test
    public void newTrigger() {
        TriggerInfo info = mock(TriggerInfo.class);
        assertNotNull(manager.newInstance(info, "#MESSAGE \"Hello World\""));
    }
}