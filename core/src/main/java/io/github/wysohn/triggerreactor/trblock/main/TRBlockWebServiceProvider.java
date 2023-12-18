package io.github.wysohn.triggerreactor.trblock.main;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.wysohn.triggerreactor.core.main.IPluginLifecycle;
import io.github.wysohn.triggerreactor.trblock.handlers.MainPageHandler;
import io.github.wysohn.triggerreactor.trblock.handlers.TRBlockHandlerImpl;
import io.javalin.Javalin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.logging.Logger;

@Singleton
public class TRBlockWebServiceProvider implements IPluginLifecycle {

    @Inject
    @Named("PluginClassLoader")
    private ClassLoader pluginClassLoader;

    @Inject
    @Named("PluginLogger")
    private Logger logger;

    @Inject
    TRBlockWebServiceImpl webService;

    private boolean isStartedBefore = false; // test only

    private int port = 8000;

    /***
     * TRBlock start method, it should only run once
     */
    @Override
    public void initialize() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(pluginClassLoader);

        try {
            TRBlockHandlerImpl[] handlers = {
                    new MainPageHandler(),
            };

            webService.init(port);
            webService.registriesHandlers(handlers);

            webService.start();
            isStartedBefore = true;

            logger.info("TRBlock has been successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {
        webService.stop();
        logger.info("TRBlock has been successfully stoped");
    }

    public boolean isStartedBefore() {
        return isStartedBefore;
    }
}
