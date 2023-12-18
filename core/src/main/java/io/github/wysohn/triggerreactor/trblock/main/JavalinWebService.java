package io.github.wysohn.triggerreactor.trblock.main;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.wysohn.triggerreactor.trblock.handlers.TRBlockHandlerImpl;
import io.javalin.Javalin;

@Singleton
public class JavalinWebService implements TRBlockWebServiceImpl {

    private Javalin app;
    private int port = 8080;

    private boolean isStarted = false;

    @Override
    public void init(int port) {
        this.app = Javalin.create();
        this.port = port;
    }

    @Override
    public boolean start() {
        app.start(port);

        isStarted = true;
        return true;
    }

    @Override
    public boolean stop() {
        app.stop();

        isStarted = false;
        return true;
    }

    @Override
    public void registriesHandlers(TRBlockHandlerImpl... handlers) {

        for (TRBlockHandlerImpl handler : handlers) {
            switch (handler.getRequestType()) {
                case GET:       app.get(handler.getPath(), handler::handler);
                case POST:      app.post(handler.getPath(), handler::handler);
                case AFTER:     app.after(handler.getPath(), handler::handler);
                case BEFORE:    app.before(handler.getPath(), handler::handler);

                //default: throw new RuntimeException("unknown RequestType!");
            }
        }
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isStarted() {
        return false;
    }
}
