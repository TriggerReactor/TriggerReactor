package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;

public abstract class AbstractScriptEditManager extends Manager {

    public AbstractScriptEditManager(TriggerReactor plugin) {
        super(plugin);
    }

    /**
     * Start the script editor.
     * @param sender the sender to start editor
     * @param title title to be shown on the editor
     * @param script the actual script. It can be empty string
     * @param saveHandler the callback interface that allows you to save the script written by editor.
     */
    public abstract void startEdit(ICommandSender sender, String title, String script, SaveHandler saveHandler);

}