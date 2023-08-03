package io.github.wysohn.triggerreactor.core.manager.js.placeholder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.multibindings.ProvidesIntoSet;
import io.github.wysohn.triggerreactor.core.main.IPluginManagement;
import io.github.wysohn.triggerreactor.core.manager.IJavascriptFileLoader;
import io.github.wysohn.triggerreactor.core.manager.js.IJSFolderContentCopyHelper;
import io.github.wysohn.triggerreactor.core.manager.js.IScriptEngineGateway;
import io.github.wysohn.triggerreactor.core.module.MockPluginManagementModule;
import io.github.wysohn.triggerreactor.core.module.TestFileModule;
import io.github.wysohn.triggerreactor.core.module.manager.PlaceholderModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlaceholderManagerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    IPluginManagement mockPluginManagement;
    TaskSupervisor taskSupervisor;
    IJSFolderContentCopyHelper mockCopyHelper;
    IJavascriptFileLoader mockFileLoader;
    IScriptEngineGateway mockScriptEngineGateway;

    PlaceholderManager placeholderManager;

    @Before
    public void setUp() throws Exception {
        mockPluginManagement = mock(IPluginManagement.class);
        taskSupervisor = mock(TaskSupervisor.class);
        mockCopyHelper = mock(IJSFolderContentCopyHelper.class);
        mockFileLoader = mock(IJavascriptFileLoader.class);
        mockScriptEngineGateway = mock(IScriptEngineGateway.class);

        placeholderManager = Guice.createInjector(
                new MockPluginManagementModule(mockPluginManagement),
                new TestFileModule(folder),
                new PlaceholderModule(),
                new AbstractModule() {
                    @Provides
                    TaskSupervisor taskSupervisor() {
                        return taskSupervisor;
                    }

                    @ProvidesIntoSet
                    public IScriptEngineGateway provideScriptEngineGateway() {
                        return mockScriptEngineGateway;
                    }

                    @Provides
                    public IJSFolderContentCopyHelper provideJSFolderContentCopyHelper() {
                        return mockCopyHelper;
                    }

                    @Provides
                    public IJavascriptFileLoader provideJavascriptFileLoader() {
                        return mockFileLoader;
                    }
                }
        ).getInstance(PlaceholderManager.class);
    }

    @Test
    public void initialize() throws IOException {
        // arrange
        File dummyJSFile = folder.newFile("dummy.js");
        File dummyNonJSFile = folder.newFile("dummy.txt");

        Files.write(dummyJSFile.toPath(), "function dummy() { return 1; }".getBytes());
        Files.write(dummyNonJSFile.toPath(), "function dummy() { return 1; }".getBytes());

        // act
        placeholderManager.initialize();

        // assert
        verify(mockCopyHelper).copyFolderFromJar("Placeholder", folder.getRoot());
    }

    @Test
    public void reload() throws IOException {
        // arrange
        File dummyJSFile = folder.newFile("dummy.js");
        File dummyNonJSFile = folder.newFile("dummy.txt");
        File childFolder = folder.newFolder("nested");
        File dummyJSFileNested = new File(childFolder, "dummy.js");

        Files.write(dummyJSFile.toPath(), "function dummy() { return 1; }".getBytes());
        Files.write(dummyNonJSFile.toPath(), "function dummy() { return 1; }".getBytes());
        Files.write(dummyJSFileNested.toPath(), "function dummy() { return 1; }".getBytes());

        ScriptEngine scriptEngine = mock(ScriptEngine.class);

        when(mockScriptEngineGateway.getEngine()).thenReturn(scriptEngine);
        when(mockFileLoader.listFiles(any(), any())).thenReturn(new File[]{dummyJSFile,
                dummyNonJSFile,
                childFolder,
                dummyJSFileNested});

        // act
        placeholderManager.reload();

        // assert
        verify(mockFileLoader).listFiles(
                eq(new File(folder.getRoot(), PlaceholderManager.JAR_FOLDER_LOCATION)),
                any());
        assertTrue(placeholderManager.containsKey("dummy"));
        assertTrue(placeholderManager.containsKey("nested@dummy"));
        verify(mockScriptEngineGateway, times(2)).getEngine();
    }

    @Test
    public void shutdown() {
        // arrange

        // act

        // assert
    }

    @Test
    public void saveAll() {
        // arrange

        // act

        // assert
    }
}