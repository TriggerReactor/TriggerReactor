package io.github.wysohn.triggerreactor.trblock.handlers;

import io.javalin.Javalin;
import io.javalin.http.Context;

public interface TRBlockHandlerImpl {

    TRBlockWebRequestEnum getRequestType();
    String getPath();

    void handler(Context context);

}
