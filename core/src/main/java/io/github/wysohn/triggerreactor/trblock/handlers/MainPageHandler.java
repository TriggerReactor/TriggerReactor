package io.github.wysohn.triggerreactor.trblock.handlers;

import com.google.inject.Inject;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

public class MainPageHandler implements TRBlockHandlerImpl{

    @Inject
    @Named("PluginClassLoader")
    ClassLoader classLoader;


    @Override
    public TRBlockWebRequestEnum getRequestType() {
        return TRBlockWebRequestEnum.GET;
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public void handler(Context context) {
        InputStream htmlContent = classLoader.getResourceAsStream("/frontend/index.html");
        if (htmlContent != null) {
            context.result(htmlContent).contentType("text/html");
        } else {
            throw new NotFoundResponse();
        }
    }
}
