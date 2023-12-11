package io.github.wysohn.triggerreactor.trblock.main;

import io.github.wysohn.triggerreactor.core.manager.Manager;
import io.javalin.Javalin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.logging.Logger;

@Singleton
public class TRBlockManager {

    @Inject
    @Named("PluginLogger")
    private Logger logger;

    private boolean isStarted = false;
    private boolean isStartedBefore = false; // test only
    private final Javalin app = Javalin.create();


    public TRBlockManager() {

    }

    /***
     * TRBlock start method
     * it should only run once, otherwise it outputs false.
     * @return is TRBlock Successfully start
     */
    public boolean start(int port) {
        if (!isStarted) {
            app.start(port);

            // handlers
            app.get("/", ctx -> ctx.result("Hello World!"));

            isStarted = true;
            isStartedBefore = true;
            logger.info("TRBlock has been successfully started.");

            return true;
        } else {
            return false;
        }
    }

    public boolean stop() {
        if (isStarted) {
            app.stop();
            logger.info("TRBlock has been successfully stoped");

            return true;
        } else {
            return false;
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isStartedBefore() {
        return isStartedBefore;
    }

}
