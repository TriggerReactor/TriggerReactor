package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.main.IPluginLifecycleController;
import io.github.wysohn.triggerreactor.core.manager.trigger.share.api.AbstractAPISupport;
import io.github.wysohn.triggerreactor.core.scope.APIScope;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Logger;

@APIScope
public class ExternalAPIManager extends Manager {
    @Inject
    Logger logger;
    @Inject
    Map<String, Class<? extends AbstractAPISupport>> externalAPIProtoMap;
    @Inject
    IPluginLifecycleController pluginLifecycleController;
    private final Map<String, AbstractAPISupport> externalAPIMap = new HashMap<>();


    @Inject
    ExternalAPIManager() {
    }

    @Override
    public void onDisable() {
        externalAPIMap.forEach((key, val) -> val.onReload());
    }

    @Override
    public void onEnable() throws Exception {
        onReload();
    }

    @Override
    public void onReload() throws RuntimeException {
        externalAPIMap.clear();
        //TODO test if loop continues when exception is thrown
        for (Map.Entry<String, Class<? extends AbstractAPISupport>> entry : externalAPIProtoMap.entrySet()) {
            if (!pluginLifecycleController.isEnabled(entry.getKey())) {
                logger.info(entry.getKey() + " is not available or not enabled.");
                continue;
            }

            AbstractAPISupport api = null;
            try {
                Object targetPluginInstance = Optional.of(entry.getKey())
                        .map(pluginLifecycleController::getPlugin)
                        .orElseThrow(RuntimeException::new);

                api = instantiate(entry.getValue(), targetPluginInstance);
                externalAPIMap.put(Objects.requireNonNull(api.getVariableName()), api);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            logger.info("Enabled support for " + api);
        }
        //TODO test if apis are there

        externalAPIMap.forEach((key, val) -> val.onReload());
        //TODO test if onReload is called
    }

    private AbstractAPISupport instantiate(Class<? extends AbstractAPISupport> clazz,
                                           Object targetPluginInstance) throws Exception {
        Constructor<?> con = null;
        try {
            con = clazz.getConstructor(Object.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
        }

        AbstractAPISupport apiSupport = (AbstractAPISupport) con.newInstance(targetPluginInstance);
        apiSupport.onEnable();

        return apiSupport;
    }

    /**
     * Get unmodifiable map of external apis.
     * <p>
     * Maps variable name -> AbstractAPISupport
     *
     * @return
     */
    public Map<String, AbstractAPISupport> getExternalAPIMap() {
        return Collections.unmodifiableMap(externalAPIMap);
    }
}
