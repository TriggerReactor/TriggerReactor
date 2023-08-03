package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.core.manager.trigger.ITriggerDependencyFacade;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BukkitTest {
    @Inject
    public ITriggerDependencyFacade dependencyFacade;

    @Inject
    private BukkitTest() {

    }

    public void verifySharedVariable(String variableName) {
        ValidationUtil.notNull(dependencyFacade.getExtraVariables(null).get(variableName));
    }
}
