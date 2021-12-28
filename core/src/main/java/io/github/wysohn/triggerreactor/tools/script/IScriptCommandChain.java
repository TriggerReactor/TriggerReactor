package io.github.wysohn.triggerreactor.tools.script;

@FunctionalInterface
public interface IScriptCommandChain {
    /**
     * Invoked by event handler upon the player chat event.
     *
     * @param user    the user
     * @param editor  the current editor
     * @param command the full command (the chat entered). Note that the command
     *                doesn't necessarily start with the slash(/) in the script editor,
     *                so no preprocessing is required unless needed.
     * @return true if this chain handled the command. No subsequent chain will be tried;
     * false to move on to the next chain
     */
    boolean onChat(ScriptEditorUser user, ScriptEditor editor, String command);
}
