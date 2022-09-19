package io.github.wysohn.triggerreactor.core.manager.trigger.area;

import io.github.wysohn.triggerreactor.core.config.InvalidTrgConfigurationException;
import io.github.wysohn.triggerreactor.core.config.source.ConfigSourceFactory;
import io.github.wysohn.triggerreactor.core.config.source.IConfigSource;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.AbstractTriggerManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerConfigKey;
import io.github.wysohn.triggerreactor.core.manager.trigger.TriggerInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class AreaTriggerLoaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TriggerReactorCore core;
    AreaTriggerLoader loader;

    @Before
    public void setUp() throws Exception {
        core = mock(TriggerReactorCore.class, RETURNS_DEEP_STUBS);
        Field instanceField = TriggerReactorCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, core);

        when(core.getExecutorManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getPlaceholderManager().getBackedMap()).thenReturn(new HashMap<>());
        when(core.getVariableManager().getGlobalVariableAdapter()).thenReturn(new HashMap<>());
        when(core.getDataFolder()).thenReturn(folder.getRoot());

        loader = new AreaTriggerLoader(core);
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
        File areaTrigger = folder.newFolder("trigger1");
        File configFile = folder.newFile("trigger1.json");
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
        File areaTriggerFolder1 = folder.newFolder("trigger1");

        IConfigSource source = mock(IConfigSource.class);
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), String.class))
                .thenReturn(Optional.of("world@0,0,0"));
        when(source.get(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), String.class))
                .thenReturn(Optional.of("world@10,10,10"));

        TriggerInfo info = new AreaTriggerInfo(areaTriggerFolder1, source, "trigger1");
        AreaTrigger trigger = loader.load(info);
        trigger.setEnterTrigger("enter");
        trigger.setExitTrigger("exit");

        loader.save(trigger);

        verify(source).put(TriggerConfigKey.KEY_TRIGGER_AREA_SMALLEST.getKey(), "world@0,0,0");
        verify(source).put(TriggerConfigKey.KEY_TRIGGER_AREA_LARGEST.getKey(), "world@10,10,10");

        assertEquals("enter", fileContent(new File(areaTriggerFolder1,
                                                   AreaTriggerLoader.TRIGGER_NAME_ENTER + ".trg")));
        assertEquals("exit", fileContent(new File(areaTriggerFolder1,
                                                  AreaTriggerLoader.TRIGGER_NAME_EXIT + ".trg")));
    }

    private String fileContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}