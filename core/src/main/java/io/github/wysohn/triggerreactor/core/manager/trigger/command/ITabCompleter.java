package io.github.wysohn.triggerreactor.core.manager.trigger.command;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The tab completer interface, which is responsible for handling tab completion process. Each tab completer must handle
 * tab completion request per argument index. For example, 0th argument has its own ITabCompleter, 1st argument has
 * another ITabCompleter, and so on.
 */
public interface ITabCompleter {

    /**
     * Get possible candidates commands, which can fit into the current argument the player is about to enter.
     * This only works when player enters command at least 1 character, and naturally, the method is expected to return
     * only the possible candidates that starts with the given character(s) to minimize the overhead.
     * <br><br>
     * For example, if a player enter `/mycmd abc def g' in the command line, the candidate commands at argument
     * index 2 that
     * are starting with `g` will show up to the user as tab completion selections.
     *
     * @param part the partially completed command.
     * @return list of candidates which can fit
     */
    List<String> getCandidates(String part);

    /**
     * convert to string so it can be saved to the config source
     * @return
     */
    String asConfigString();

    static List<String> list(String... strings) {
        return Arrays.stream(strings).collect(Collectors.toList());
    }


    enum Template {
        EMPTY,
        PLAYER
    }

}