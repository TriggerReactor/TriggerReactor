package io.github.wysohn.triggerreactor.core.manager.trigger.share.api;

public class SampleAPISupport extends AbstractAPISupport{

    public SampleAPISupport(Object targetPluginInstance) {
        super(targetPluginInstance);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() throws RuntimeException {

    }

    @Override
    public String getVariableName() {
        return "sample";
    }
}
