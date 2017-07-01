package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

public abstract class AbstractScriptEditManager extends Manager {

    public AbstractScriptEditManager(TriggerReactor plugin) {
        super(plugin);
    }

    public abstract void startEdit(IPlayer sender, String title, String script, SaveHandler saveHandler);

}