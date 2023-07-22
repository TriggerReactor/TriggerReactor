package io.github.wysohn.triggerreactor.bukkit.main;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.ProvidesIntoMap;
import com.google.inject.multibindings.StringMapKey;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.ICommandMapHandler;
import io.github.wysohn.triggerreactor.bukkit.manager.trigger.share.api.APISupport;
import io.github.wysohn.triggerreactor.bukkit.modules.BukkitDriverModule;
import io.github.wysohn.triggerreactor.core.manager.js.executor.ExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.js.placeholder.PlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.APISupportException;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class JavaPluginTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Server server;
    private Logger logger;
    private PluginManager pluginManager;
    private ICommandMapHandler commandMapHandler;
    private SelfReference selfReference;

    private JavaPluginLoader loader;
    private PluginDescriptionFile description;
    private AbstractJavaPlugin abstractJavaPlugin;

    private void injectServerToBukkit() throws Exception {
        Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
        Field field = bukkitClass.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, server);
    }

    private void cleanServer() throws Exception {
        Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
        Field field = bukkitClass.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Before
    public void setUp() throws Exception {
        server = mock(Server.class, RETURNS_DEEP_STUBS);
        logger = Logger.getLogger("test");
        pluginManager = mock(PluginManager.class);
        List<World> worlds = new ArrayList<>();
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getLogger()).thenReturn(logger);
        when(server.getWorlds()).thenReturn(worlds);
        injectServerToBukkit();

        String pluginYmlContent = "" +
                "name: TriggerReactor\n" +
                "main: io.github.wysohn.triggerreactor.bukkit.main.TriggerReactor\n" +
                "version: test\n";
        File dataFolder = temporaryFolder.newFolder("plugins", "TriggerReactor");
        File file = temporaryFolder.newFile("TriggerReactor.jar");
        File executorFolder = temporaryFolder.newFolder("plugins",
                "TriggerReactor",
                ExecutorManager.JAR_FOLDER_LOCATION);
        File placeholderFolder = temporaryFolder.newFolder("plugins",
                "TriggerReactor",
                PlaceholderManager.JAR_FOLDER_LOCATION);

        loader = new JavaPluginLoader(server);
        description = new PluginDescriptionFile(new ByteArrayInputStream(pluginYmlContent.getBytes()));
        commandMapHandler = mock(ICommandMapHandler.class);
        selfReference = mock(SelfReference.class);

        abstractJavaPlugin = new TestPlugin(loader,
                description,
                dataFolder,
                file,
                new BukkitDriverModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ICommandMapHandler.class).toInstance(commandMapHandler);
                        bind(SelfReference.class).toInstance(selfReference);
                    }

                    @ProvidesIntoMap
                    @StringMapKey("placeholder")
                    public Class<? extends AbstractAPISupport> providePlaceholderSupport() {
                        return PlaceHolderSupport.class;
                    }
                });
    }

    @After
    public void tearDown() throws Exception {
        cleanServer();
    }

    @Test
    public void onEnable() {
        // arrange

        // act
        abstractJavaPlugin.onEnable();

        // assert
        abstractJavaPlugin.test.verifySharedVariable("placeholder");
    }

    private static class TestPlugin extends AbstractJavaPlugin {
        public TestPlugin(JavaPluginLoader loader,
                          PluginDescriptionFile description,
                          File dataFolder,
                          File file,
                          Module... modules) {
            super(loader, description, dataFolder, file, modules);
        }
    }

    public static class PlaceHolderSupport extends APISupport {
        public PlaceHolderSupport(Injector injector) {
            super(injector, "PlaceholderAPI");
        }

        @Override
        public void init() throws APISupportException {

        }
    }
}