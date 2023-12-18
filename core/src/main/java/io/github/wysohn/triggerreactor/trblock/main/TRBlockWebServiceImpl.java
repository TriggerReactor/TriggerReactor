package io.github.wysohn.triggerreactor.trblock.main;

import io.github.wysohn.triggerreactor.trblock.handlers.TRBlockHandlerImpl;

public interface TRBlockWebServiceImpl {

    void init(int port);
    boolean start();
    boolean stop();

    void registriesHandlers(TRBlockHandlerImpl... handlers);

    int getPort();
    boolean isStarted();

}
