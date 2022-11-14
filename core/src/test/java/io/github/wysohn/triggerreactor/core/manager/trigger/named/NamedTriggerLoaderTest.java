/*
 * Copyright (C) 2022. TriggerReactor Team
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

package io.github.wysohn.triggerreactor.core.manager.trigger.named;

import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NamedTriggerLoaderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    NamedTriggerLoader loader;

    @Before
    public void init() throws IllegalAccessException, NoSuchFieldException {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        loader = Guice.createInjector().getInstance(NamedTriggerLoader.class);
    }

    @Test
    public void listTriggers() throws IOException {
        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        TriggerInfo[] loaded = loader.listTriggers(folder.getRoot(), ConfigSourceFactory.instance());

        assertEquals(1, loaded.length);
        assertEquals("test", loaded[0].getTriggerName());
    }

    @Test
    public void listTriggersRecursive() throws IOException {
        File subFolder = folder.newFolder("sub");
        File subsubFolder = folder.newFolder("sub", "subsub");
        File file2 = new File(subFolder, "test2.trg");
        File file3 = new File(subsubFolder, "test3.trg");
        file2.createNewFile();
        file3.createNewFile();

        TriggerInfo info2 = mock(TriggerInfo.class);
        when(info2.getTriggerName()).thenReturn("test2");
        when(info2.getSourceCodeFile()).thenReturn(file2);
        TriggerInfo info3 = mock(TriggerInfo.class);
        when(info3.getTriggerName()).thenReturn("test3");
        when(info3.getSourceCodeFile()).thenReturn(file3);

        TriggerInfo[] loaded = loader.listTriggers(folder.getRoot(), ConfigSourceFactory.instance());

        assertEquals(2, loaded.length);
        assertTrue(isIn(loaded, "sub:test2"));
        assertTrue(isIn(loaded, "sub:subsub:test3"));
    }

    private boolean isIn(TriggerInfo[] infos, String name){
        for(TriggerInfo info : infos){
            if(info.getTriggerName().equals(name))
                return true;
        }

        return false;
    }

    @Test
    public void load() throws IOException, InvalidTrgConfigurationException {
        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        NamedTrigger trigger = loader.load(info);

        // There is nothing much to do here. Just make sure it doesn't throw any exception.
    }

    @Test
    public void save() throws IOException, InvalidTrgConfigurationException {
        TriggerInfo info = mock(TriggerInfo.class);
        when(info.getTriggerName()).thenReturn("test");
        when(info.getSourceCodeFile()).thenReturn(folder.newFile("test.trg"));

        loader.load(info);
        // N/A
    }
}