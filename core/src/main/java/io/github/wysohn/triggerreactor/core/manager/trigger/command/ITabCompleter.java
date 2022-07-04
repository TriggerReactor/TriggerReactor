package io.github.wysohn.triggerreactor.core.manager.trigger.command;



import java.util.*;
import java.util.regex.Pattern;
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

    /**
     * Get whether this tab-completer is expected to tab-complete with pre-defined values like player list. If the
     * tab-completer instance does have valid {@linkplain TabCompleterPreDefinedValue} value that can be got with
     * {@link #getPreDefinedValue()} method, this method will return true. If not, returns false.
     * This method is needed because if the tab-completer have to tab-complete with pre-defined value, Candidate
     * should be ignored and therefore indicator of it is required.
     * <br><br>
     *
     * @return Whether the tab-completer has valid {@linkplain TabCompleterPreDefinedValue} or not
     */
    boolean isPreDefinedValue();

    /**
     * Get  {@linkplain TabCompleterPreDefinedValue} value of the tab-completer.
     * This method is needed because if the tab-completer have to tab-complete with pre-defined value, Candidate
     * should be ignored and therefore indicator of it is required.
     * <br><br>
     *
     * @return {@linkplain TabCompleterPreDefinedValue} of the tab-completer. If not being defined, returns null
     */
    TabCompleterPreDefinedValue getPreDefinedValue();

    /**
     * Get whether there is any condition to satisfy.
     * <br><br>
     *
     * @return whether there is any condition to satisfy or not
     */
    boolean hasConditionMap();


    /**
     * Get the Map of condition. The Key is parameter index, and Value is Regex string.
     * <br><br>
     *
     * @return Condtion {@linkplain java.util.Map} of the tab-completer. If not being defined, returns null
     */
    Map<Integer, Pattern> getConditionMap();

    /**
     * Get whether there is a condition to satisfy with the argument of given index.
     * <br><br>
     *
     * @return whether there is any condition to satisfy with the argument of given index or not
     */
    boolean hasCondition(Integer index);


    /**
     * Get the Regex {@linkplain java.util.regex.Pattern} of the condition to satisfy with the argument
     * of given index.
     * <br><br>
     *
     * @return Condtion's Regex {@linkplain java.util.regex.Pattern} of the tab-completer. If not being defined, returns null
     */
    Pattern getCondition(Integer index);


    class Builder{

        List<String> hint,candidate;
        TabCompleterPreDefinedValue preDefinedValue;
        Map<Integer, Pattern> conditions;

        private Builder(){
            this.hint = new ArrayList<>();
            this.candidate = new ArrayList<>();
        }

        private Builder(String... arg){
            this.hint = Arrays.stream(arg)
                    .collect(Collectors.toList());
            this.candidate = this.hint;
        }

        private Builder(Template template){
            if(template == Template.EMPTY){
                this.hint = new ArrayList<>();
                this.candidate = new ArrayList<>();
            }
            if(template == Template.PLAYER){
                this.hint = null;
                this.candidate = null; // returning null signals to list online players instead
                this.preDefinedValue = TabCompleterPreDefinedValue.PLAYERS;
                //TODO not sure if Sponge does the same
            }
        }

        public static Builder of(String... arg){
            return new Builder(arg);
        }

        public static Builder of(Template template){
            return new Builder(template);
        }

        public static Builder withHint(String hint){
            return Builder.of().appendHint(hint);
        }

        public Builder appendHint(String... hints){
            this.hint.addAll(list(hints));

            return this;
        }
        public Builder appendHint(Collection<String> hints){
            this.hint.addAll(hints);

            return this;
        }
        public Builder appendCandidate(String... candidates){
            this.candidate.addAll(list(candidates));

            return this;
        }
        public Builder appendCandidate(Collection<String> candidates){
            this.candidate.addAll(candidates);

            return this;
        }
        public Builder setHint(String...hints){
            this.hint = list(hints);

            return this;
        }
        public Builder setHint(Collection<String> hints){
            this.hint = hints instanceof List ? (List<String>) hints : new ArrayList<>(hints);

            return this;
        }
        public Builder setCandidate(String...candidates){
            this.candidate = list(candidates);

            return this;
        }
        public Builder setCandidate(Collection<String> candidates){
            if(candidates == null)
                this.candidate = new ArrayList<>();
            else
                this.candidate = candidates instanceof List ? (List<String>) candidates : new ArrayList<>(candidates);

            return this;
        }
        public Builder setPreDefinedValue(TabCompleterPreDefinedValue val){
            this.preDefinedValue = val;
            return this;
        }

        public Builder reset(){
            this.hint = new ArrayList<>();
            this.candidate = new ArrayList<>();
            this.preDefinedValue = null;
            this.conditions = null;

            return this;
        }

        public Builder setConditions(Map<Integer, Pattern> conditionMap){
            this.conditions = conditionMap;

            return this;
        }


        public ITabCompleter build(){
            return new ITabCompleterImpl(this);

        }

    }


    static List<String> list(String... strings) {
        return Arrays.stream(strings).collect(Collectors.toList());
    }

    enum Template{
        EMPTY,PLAYER
    }
}