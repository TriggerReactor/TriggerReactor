package io.github.wysohn.triggerreactor.core.config.source;

import com.google.inject.Guice;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ConfigSourceFactoryTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    IPluginManagement pluginManagement;
    IConfigSourceFactory configSourceFactory;

    @Before
    public void init() {
        pluginManagement = mock(IPluginManagement.class);

        configSourceFactory = Guice.createInjector(
                new FactoryModuleBuilder()
                        .implement(IConfigSource.class, GsonConfigSource.class)
                        .build(IConfigSourceFactory.class)
        ).getInstance(IConfigSourceFactory.class);
    }

    @Test
    public void create() throws IOException {
        // arrange
        File configFile = folder.newFile("test.json");
        Files.write(configFile.toPath(), ("{" +
                "\"first\": 123," +
                "\"second\": \"abc\"," +
                "\"third\": true" +
                "}").getBytes());
        SaveWorker saveWorker = new SaveWorker(5, mock(Consumer.class));

        // act
        IConfigSource configSource = configSourceFactory.create(saveWorker, folder.getRoot(), "test");

        configSource.reload();

        // assert
        assertEquals(123, (int) configSource.get("first", Integer.class).orElseThrow(RuntimeException::new));
        assertEquals("abc", configSource.get("second", String.class).orElseThrow(RuntimeException::new));
        assertEquals(true, configSource.get("third", Boolean.class).orElseThrow(RuntimeException::new));
    }
}
