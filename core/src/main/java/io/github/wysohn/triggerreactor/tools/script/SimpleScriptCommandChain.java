package io.github.wysohn.triggerreactor.tools.script;

public class SimpleScriptCommandChain implements IScriptCommandChain {
    private final String targetCommand;
    private final ISimpleCommandHandle chain;

    public SimpleScriptCommandChain(String targetCommand, ISimpleCommandHandle chain) {
        this.targetCommand = targetCommand;
        this.chain = chain;
    }

    @Override
    public boolean onChat(ScriptEditorUser user, ScriptEditor editor, String command) {
        if (!command.equals(targetCommand))
            return false;

        chain.onChat(user, editor, command);
        return true;
    }

    public interface ISimpleCommandHandle {
        void onChat(ScriptEditorUser user, ScriptEditor editor, String command);
    }
}
