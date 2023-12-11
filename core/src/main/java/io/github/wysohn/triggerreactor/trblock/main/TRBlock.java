package io.github.wysohn.triggerreactor.trblock.main;

import io.github.wysohn.triggerreactor.core.main.IPluginLifecycle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class TRBlock implements IPluginLifecycle {

    @Inject
    @Named("PluginClassLoader")
    private ClassLoader pluginClassLoader;
    @Inject
    private TRBlockManager manager;

    private int port = 8080;
    @Override
    public void initialize() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(pluginClassLoader);

        manager.start(port);

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {
        manager.stop();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
