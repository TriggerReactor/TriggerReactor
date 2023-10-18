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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.SaveWorker;
import io.github.wysohn.triggerreactor.core.manager.location.Area;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.TestTriggerDependencyModule;
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
import static org.mockito.Mockito.*;

public class AreaTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    IAreaTriggerFactory factory;

    AreaTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        factory = mock(IAreaTriggerFactory.class);

        loader = Guice.createInjector(
                new TestFileModule(folder),
                TestTriggerDependencyModule.Builder.begin().build(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(IAreaTriggerFactory.class).toInstance(factory);
                    }
                }
        ).getInstance(AreaTriggerLoader.class);
    }

    private String fileContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void writeContent(File file, String s) throws IOException {
        Files.write(file.toPath(), s.getBytes(StandardCharsets.UTF_8));
    }

    private String generateLongText(int length) {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < length; i++) {
            longText.append("This is a very long text content. ");
        }
        return longText.toString();
    }

    @Test
    public void listTriggers() throws IOException {
        IConfigSourceFactory factory = mock(IConfigSourceFactory.class);
        File triggerFolder = folder.newFolder("trigger");
        IConfigSource source = mock(IConfigSource.class);
        SaveWorker saveWorker = mock(SaveWorker.class);

        File enterFile = new File(triggerFolder, AreaTriggerLoader.TRIGGER_NAME_ENTER + ".trg");
        enterFile.createNewFile();
        File exitFile = new File(triggerFolder, AreaTriggerLoader.TRIGGER_NAME_EXIT + ".trg");
        exitFile.createNewFile();

        when(factory.create(saveWorker, folder.getRoot(), "trigger")).thenReturn(source);

        TriggerInfo[] triggerInfos = loader.listTriggers(saveWorker, folder.getRoot(), factory);

        assertEquals(1, triggerInfos.length);
        assertEquals("trigger", triggerInfos[0].getTriggerName());
        verify(factory).create(saveWorker, folder.getRoot(), "trigger");
    }

    @Test
    public void load() throws InvalidTrgConfigurationException, IOException, AbstractTriggerManager.TriggerInitFailedException {
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
        AreaTrigger trigger = mock(AreaTrigger.class);
        when(factory.create(any(), any(), any())).thenReturn(trigger);

        loader.load(info);

        verify(factory).create(any(), eq(new Area(
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10)
        )), any());
        verify(trigger).setEnterTrigger(eq("#MESSAGE \"enter\""));
        verify(trigger).setExitTrigger(eq("#MESSAGE \"exit\""));
    }

    @Test
    public void save() throws InvalidTrgConfigurationException, IOException,
            AbstractTriggerManager.TriggerInitFailedException {
        File areaTriggerFolder = folder.newFolder("AreaTrigger");
        File areaTrigger1 = new File(areaTriggerFolder, "trigger1");
        Area area = new Area(
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10)
        );

        IConfigSource source = mock(IConfigSource.class);
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), String.class))
                .thenReturn(Optional.of("world@0,0,0"));
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), String.class))
                .thenReturn(Optional.of("world@10,10,10"));

        TriggerInfo info = new AreaTriggerInfo(areaTrigger1, source, "trigger1");

        AreaTrigger trigger = mock(AreaTrigger.class);
        EnterTrigger enterTrigger = mock(EnterTrigger.class);
        ExitTrigger exitTrigger = mock(ExitTrigger.class);
        when(trigger.getEnterTrigger()).thenReturn(enterTrigger);
        when(trigger.getExitTrigger()).thenReturn(exitTrigger);
        when(enterTrigger.getScript()).thenReturn("enter");
        when(exitTrigger.getScript()).thenReturn("exit");
        when(trigger.getArea()).thenReturn(area);
        when(trigger.getInfo()).thenReturn(info);

        loader.save(trigger);

        assertEquals("enter", fileContent(new File(areaTrigger1,
                AreaTriggerLoader.TRIGGER_NAME_ENTER + ".trg")));
        assertEquals("exit", fileContent(new File(areaTrigger1,
                AreaTriggerLoader.TRIGGER_NAME_EXIT + ".trg")));
    }

    @Test
    public void testConcurrentSave() throws InterruptedException, IOException {
        String longText = generateLongText(1000000);

        File areaTriggerFolder = folder.newFolder("AreaTrigger");
        File areaTrigger1 = new File(areaTriggerFolder, "trigger1");
        Area area = new Area(
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world", 10, 10, 10)
        );

        IConfigSource source = mock(IConfigSource.class);
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), String.class))
                .thenReturn(Optional.of("world@0,0,0"));
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), String.class))
                .thenReturn(Optional.of("world@10,10,10"));

        TriggerInfo info = new AreaTriggerInfo(areaTrigger1, source, "trigger1");

        AreaTrigger trigger = mock(AreaTrigger.class);
        EnterTrigger enterTrigger = mock(EnterTrigger.class);
        ExitTrigger exitTrigger = mock(ExitTrigger.class);
        when(trigger.getEnterTrigger()).thenReturn(enterTrigger);
        when(trigger.getExitTrigger()).thenReturn(exitTrigger);
        when(enterTrigger.getScript()).thenReturn(longText);
        when(exitTrigger.getScript()).thenReturn(longText);
        when(trigger.getArea()).thenReturn(area);
        when(trigger.getInfo()).thenReturn(info);

        int numThreads = 50; // Number of concurrent threads
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                try {
                    loader.save(trigger);
                } catch (Exception e) {
                    // Handle exceptions if needed
                    e.printStackTrace();
                }
            });
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join(); // Wait for all threads to finish
        }

        // After all threads finish, check the content of the file
        assertEquals(longText, fileContent(new File(areaTrigger1,
                AreaTriggerLoader.TRIGGER_NAME_ENTER + ".trg")));
        assertEquals(longText, fileContent(new File(areaTrigger1,
                AreaTriggerLoader.TRIGGER_NAME_EXIT + ".trg")));
    }

    @Test
    public void save_concurrent() throws InvalidTrgConfigurationException, IOException,
            AbstractTriggerManager.TriggerInitFailedException {
        File areaTriggerFolder = folder.newFolder("AreaTrigger");
        File areaTrigger1 = new File(areaTriggerFolder, "trigger1");
        Area area = new Area(
                new SimpleLocation("world", 0, 0, 0),
                new SimpleLocation("world ", 10, 10, 10)
        );


    }

}
