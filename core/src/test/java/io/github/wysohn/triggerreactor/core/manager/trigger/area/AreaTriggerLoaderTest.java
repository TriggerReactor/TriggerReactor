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

package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.module.manager.trigger.AreaTriggerModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class AreaTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    AreaTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = Guice.createInjector(
                new AreaTriggerModule()
        ).getInstance(AreaTriggerLoader.class);
    }

    @Test
    public void listTriggers() throws IOException {
        ConfigSourceFactory factory = mock(ConfigSourceFactory.class);
        File triggerFolder = folder.newFolder("trigger");
        IConfigSource source = mock(IConfigSource.class);

        File enterFile = new File(triggerFolder, AreaTriggerLoader.TRIGGER_NAME_ENTER + ".trg");
        enterFile.createNewFile();
        File exitFile = new File(triggerFolder, AreaTriggerLoader.TRIGGER_NAME_EXIT + ".trg");
        exitFile.createNewFile();

        when(factory.create(folder.getRoot(), "trigger")).thenReturn(source);

        TriggerInfo[] triggerInfos = loader.listTriggers(folder.getRoot(), factory);

        assertEquals(1, triggerInfos.length);
        assertEquals("trigger", triggerInfos[0].getTriggerName());
        verify(factory).create(folder.getRoot(), "trigger");
    }

    private void writeContent(File file, String s) throws IOException {
        Files.write(file.toPath(), s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void load() throws InvalidTrgConfigurationException, IOException {
        File areaTriggerFolder = folder.newFolder("AreaTrigger");
        File areaTrigger = new File(areaTriggerFolder, "trigger1");
        File configFile = new File(areaTriggerFolder, "trigger1.json");
        areaTrigger.mkdirs();
        configFile.createNewFile();

        IConfigSource source = mock(IConfigSource.class);
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), String.class))
                .thenReturn(Optional.of("world@0,0,0"));
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), String.class))
                .thenReturn(Optional.of("world@10,10,10"));
        File enterFile = new File(areaTrigger, AreaTriggerLoader.TRIGGER_NAME_ENTER + ".trg");
        enterFile.createNewFile();
        writeContent(enterFile, "#MESSAGE \"enter\"");
        File exitFile = new File(areaTrigger, AreaTriggerLoader.TRIGGER_NAME_EXIT + ".trg");
        exitFile.createNewFile();
        writeContent(exitFile, "#MESSAGE \"exit\"");

        TriggerInfo info = new AreaTriggerInfo(areaTrigger, source, "trigger1");
        AreaTrigger trigger = loader.load(info);

        assertNotNull(trigger);
        assertEquals(new SimpleLocation("world", 0, 0, 0), trigger.getArea().getSmallest());
        assertEquals(new SimpleLocation("world", 10, 10, 10), trigger.getArea().getLargest());

        assertEquals("#MESSAGE \"enter\"", trigger.getEnterTrigger().getScript());
        assertEquals("#MESSAGE \"exit\"", trigger.getExitTrigger().getScript());
    }

    @Test
    public void save() throws InvalidTrgConfigurationException, IOException,
            AbstractTriggerManager.TriggerInitFailedException {
        File areaTriggerFolder = folder.newFolder("AreaTrigger");
        File areaTrigger1 = new File(areaTriggerFolder, "trigger1");

        IConfigSource source = mock(IConfigSource.class);
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), String.class))
                .thenReturn(Optional.of("world@0,0,0"));
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), String.class))
                .thenReturn(Optional.of("world@10,10,10"));

        TriggerInfo info = new AreaTriggerInfo(areaTrigger1, source, "trigger1");
        AreaTrigger trigger = loader.load(info);
        trigger.setEnterTrigger("enter");
        trigger.setExitTrigger("exit");

        loader.save(trigger);

        verify(source).put(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), "world@0,0,0");
        verify(source).put(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), "world@10,10,10");

        assertEquals("enter", fileContent(new File(areaTrigger1,
                                                   AreaTriggerLoader.TRIGGER_NAME_ENTER + ".trg")));
        assertEquals("exit", fileContent(new File(areaTrigger1,
                                                  AreaTriggerLoader.TRIGGER_NAME_EXIT + ".trg")));
    }

    private String fileContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}