package io.github.wysohn.triggerreactor.core.main.command;

public class RootCommand extends CommandComposite {
    RootCommand(Usage usage) {
        super("<root>", usage, true);
    }

//    void pluginDescription(){
//        sender.sendMessage(
//                "&7-----     &6" + TriggerReactorAPI.pluginLifecycleController().getPluginDescription() + "&7    ----");
//        for (TriggerCommand child : children) {
//            child.printUsage(sender, depth - 1);
//        }
//        sender.sendMessage("");
//    }
}
