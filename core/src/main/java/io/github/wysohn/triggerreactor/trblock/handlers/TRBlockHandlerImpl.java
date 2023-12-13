package io.github.wysohn.triggerreactor.trblock.handlers;

import io.javalin.Javalin;
import io.javalin.http.Context;

public interface TRBlockHandlerImpl {

    TRBlockWebRequestEnum getRequestType();
    String getPath();

    void handler(Context context);

    default void add(Javalin app) {
        System.out.println(getRequestType().toString());
        switch (getRequestType()) {
            case GET:       app.get(getPath(), this::handler);
            case POST:      app.post(getPath(), this::handler);
            case AFTER:     app.after(getPath(), this::handler);
            case BEFORE:    app.before(getPath(), this::handler);

            //default: throw new RuntimeException("unknown RequestType!");
        }
    }

}
