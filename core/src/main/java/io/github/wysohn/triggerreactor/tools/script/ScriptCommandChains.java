package io.github.wysohn.triggerreactor.tools.script;

import java.util.LinkedList;
import java.util.List;

public class ScriptCommandChains implements IScriptCommandChain {
    private final List<IScriptCommandChain> scriptCommandChains = new LinkedList<>();

    private ScriptCommandChains() {

    }

    /**
     * Handle the chat and move it along the chain of IScriptCommandChain.
     *
     * @param user    the user
     * @param editor
     * @param command the full command (the chat entered). Note that the command
     *                doesn't necessarily start with the slash(/) in the script editor,
     *                so no preprocessing is required unless needed.
     * @return true if one of the chain handled the command; false if none of the chain handled the command
     */
    @Override
    public boolean onChat(ScriptEditorUser user, ScriptEditor editor, String command) {
        for (IScriptCommandChain commandChain : scriptCommandChains) {
            if (commandChain.onChat(user, editor, command))
                return true;
        }

        return false;
    }

    public static class Builder {
        private final ScriptCommandChains chain = new ScriptCommandChains();

        private Builder() {

        }

        public Builder chain(IScriptCommandChain commandChain) {
            if (!chain.scriptCommandChains.contains(commandChain))
                chain.scriptCommandChains.add(commandChain);
            return this;
        }

        public ScriptCommandChains build() {
            return chain;
        }

        public static Builder begin() {
            return new Builder();
        }
    }
}
