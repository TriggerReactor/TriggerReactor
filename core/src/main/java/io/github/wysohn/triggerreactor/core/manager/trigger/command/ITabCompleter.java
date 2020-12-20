package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The tab completer interface, which is responsible for handling tab completion process. Each tab completer must handle
 * tab completion request per argument index. For example, 0th argument has its own ITabCompleter, 1st argument has
 * another ITabCompleter, and so on.
 */
public interface ITabCompleter {
    ITabCompleter EMPTY = new ITabCompleter() {
        @Override
        public List<String> getCandidates(String part) {
            return new ArrayList<>();
        }

        @Override
        public List<String> getHint() {
            return new ArrayList<>();
        }
    };

    ITabCompleter PLAYER = new ITabCompleter() {
        @Override
        public List<String> getCandidates(String part) {
            return null; // returning null signals to list online players instead
            //TODO not sure if Sponge does the same
        }

        @Override
        public List<String> getHint() {
            return list("<player>");
        }
    };

    static List<String> list(String... strings) {
        return Arrays.stream(strings).collect(Collectors.toList());
    }

    static ITabCompleter hint(String hint) {
        return new ITabCompleter() {
            @Override
            public List<String> getCandidates(String part) {
                return ITabCompleter.list();
            }

            @Override
            public List<String> getHint() {
                return ITabCompleter.list(hint);
            }
        };
    }

    static ITabCompleter simple(String... arg) {
        return new ITabCompleter() {
            @Override
            public List<String> getCandidates(String part) {
                return Arrays.stream(arg)
                        .filter(val -> val.startsWith(part))
                        .collect(Collectors.toList());
            }

            @Override
            public List<String> getHint() {
                return Arrays.stream(arg)
                        .collect(Collectors.toList());
            }
        };
    }

    /**
     * Get possible candidates commands, which can fit into the current argument the player is about to enter.
     * This only works when player enters command at least 1 character, and naturally, the method is expected to return
     * only the possible candidates that starts with the given character(s) to minimize the overhead.
     * <br><br>
     * For example, if a player enter `/mycmd abc def g' in the command line, the candidate commands at argument index 2 that
     * are starting with `g` will show up to the user as tab completion selections.
     *
     * @param part the partially completed command.
     * @return list of candidates which can fit
     */
    List<String> getCandidates(String part);

    /**
     * Get simple help string that will show up to the player. Unlike {@link #getCandidates(String)}, this method
     * will be called when there is no character exist in the place of current argument.
     * <br><br>
     * For example, you may decide to show simple String, [player], so the player can effectively know what to enter
     * at the current argument index. If this method returns the same value as {@link #getCandidates(String)},
     * it will behave as same as the naive tab completer.
     *
     * @return list of String to be shown as hint.
     */
    List<String> getHint();
}
