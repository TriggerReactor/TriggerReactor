package io.github.wysohn.triggerreactor.core.manager.js.executor;

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
import io.github.wysohn.triggerreactor.core.module.manager.ExecutorModule;
import io.github.wysohn.triggerreactor.core.module.manager.PlaceholderModule;
import io.github.wysohn.triggerreactor.core.script.interpreter.Executor;
import io.github.wysohn.triggerreactor.core.script.interpreter.Placeholder;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ExecutorManagerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    ScriptEngineManager sem;

    Executor mockExecutor;
    Placeholder mockPlaceholder;
    IJSExecutorFactory mockFactory;
    IPluginManagement mockPluginManagement;
    TaskSupervisor taskSupervisor;
    IJSFolderContentCopyHelper mockCopyHelper;
    IJavascriptFileLoader mockFileLoader;
    IScriptEngineGateway mockScriptEngineGateway;

    ExecutorManager executorManager;


    @Before
    public void setUp() throws Exception {
        sem = new ScriptEngineManager();

        mockExecutor = mock(Executor.class);
        mockPlaceholder = mock(Placeholder.class);
        mockFactory = mock(IJSExecutorFactory.class);
        mockPluginManagement = mock(IPluginManagement.class);
        taskSupervisor = mock(TaskSupervisor.class);
        mockCopyHelper = mock(IJSFolderContentCopyHelper.class);
        mockFileLoader = mock(IJavascriptFileLoader.class);
        mockScriptEngineGateway = mock(IScriptEngineGateway.class);

        executorManager = Guice.createInjector(
                new MockPluginManagementModule(mockPluginManagement),
                new TestFileModule(folder),
                new ExecutorModule(),
                new PlaceholderModule(),
                new AbstractModule() {
                    @Provides
                    public TaskSupervisor provideTaskSupervisor() {
                        return taskSupervisor;
                    }

                    @Provides
                    public IJSFolderContentCopyHelper provideCopyHelper() {
                        return mockCopyHelper;
                    }

                    @Provides
                    public IJavascriptFileLoader provideFileLoader() {
                        return mockFileLoader;
                    }

                    @ProvidesIntoSet
                    public IScriptEngineGateway provideScriptEngineGateway() {
                        return mockScriptEngineGateway;
                    }
                }
        ).getInstance(ExecutorManager.class);
    }

    @Test
    public void initialize() throws Exception {
        // arrange
        File dummyJSFile = folder.newFile("DUMMY.js");
        File dummyNonJSFile = folder.newFile("DUMMY.txt");

        Files.write(dummyJSFile.toPath(), "function DUMMY() { return 1; }".getBytes());
        Files.write(dummyNonJSFile.toPath(), "function DUMMY() { return 1; }".getBytes());

        // act
        executorManager.initialize();

        // assert
        verify(mockCopyHelper).copyFolderFromJar("Executor", folder.getRoot());
    }

    @Test
    public void reload() throws Exception {
        // arrange
        File dummyJSFile = folder.newFile("DUMMY.js");
        File dummyNonJSFile = folder.newFile("DUMMY.txt");
        File childFolder = folder.newFolder("nested");
        File dummyJSFileNested = new File(childFolder, "DUMMY.js");

        Files.write(dummyJSFile.toPath(), "function DUMMY() { return 1; }".getBytes());
        Files.write(dummyNonJSFile.toPath(), "function DUMMY() { return 1; }".getBytes());
        Files.write(dummyJSFileNested.toPath(), "function DUMMY() { return 1; }".getBytes());

        ScriptEngine scriptEngine = mock(ScriptEngine.class);

        when(mockScriptEngineGateway.getEngine()).thenReturn(scriptEngine);
        when(mockFileLoader.listFiles(any(), any())).thenReturn(new File[]{dummyJSFile,
                dummyNonJSFile,
                childFolder,
                dummyJSFileNested});

        // act
        executorManager.reload();

        // assert
        verify(mockFileLoader).listFiles(
                eq(new File(folder.getRoot(), ExecutorManager.JAR_FOLDER_LOCATION)),
                any());
        assertTrue(executorManager.containsKey("DUMMY"));
        assertTrue(executorManager.containsKey("nested:DUMMY"));
        verify(mockScriptEngineGateway, times(2)).getEngine();
    }

    @Test
    public void shutdown() {

    }

    @Test
    public void saveAll() {

    }


}