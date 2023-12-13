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

    private boolean isStarted = false;
    private boolean isStartedBefore = false; // test only

    private final Javalin app = Javalin.create();

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

            // init handlers
            for (TRBlockHandlerImpl handler: handlers) {
                handler.add(app);
            }

            app.start(port);

            isStarted = true;
            isStartedBefore = true;
            logger.info("TRBlock has been successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("TRBlock startup failed.");
        }

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {
        app.stop();

        isStarted = false;
        logger.info("TRBlock has been successfully stoped");
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isStartedBefore() {
        return isStartedBefore;
    }
}
