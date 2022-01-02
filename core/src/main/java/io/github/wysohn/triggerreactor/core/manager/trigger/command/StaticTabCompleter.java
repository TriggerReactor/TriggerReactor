package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import io.github.wysohn.triggerreactor.tools.trie.StringListTrie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StaticTabCompleter implements ITabCompleter{
    private final StringListTrie trie;

    public StaticTabCompleter(Iterable<String> iterable) {
        this.trie = new StringListTrie(iterable);
    }

    @Override
    public List<String> getCandidates(String part) {
        return trie.getAllStartsWith(part);
    }

    @Override
    public String asConfigString() {
        return trie.getAll().stream().reduce((a, b) -> a + "," + b).orElse("");
    }

    public static class Builder {
        private List<String> candidate;

        private boolean build = false;

        private Builder() {
            this.candidate = new ArrayList<>();
        }

        private Builder(String... arg) {
            this.candidate = Arrays.stream(arg).collect(Collectors.toList());
        }

        public Builder appendCandidate(String... candidates) {
            this.candidate.addAll(ITabCompleter.list(candidates));

            return this;
        }

        public Builder appendCandidate(Collection<String> candidates) {
            this.candidate.addAll(candidates);

            return this;
        }

        public StaticTabCompleter[] buildAsArray() {
            return new StaticTabCompleter[]{build()};
        }

        public StaticTabCompleter build() {
            // we don't want the candidates and hints List references to be
            // shared with multiple instances.
            if (build)
                throw new RuntimeException("Do not use the same builder twice.");
            build = true;

            return new StaticTabCompleter(candidate);
        }

        public Builder setAsPlayerList() {
            this.candidate = null;

            return this;
        }

        public Builder setCandidate(String... candidates) {
            this.candidate = ITabCompleter.list(candidates);

            return this;
        }

        public Builder setCandidate(Collection<String> candidates) {
            ValidationUtil.notNull(candidates);

            this.candidate = new ArrayList<>(candidates);
            return this;
        }

        @Deprecated
        public Builder setHint(String... hints) {
            return this;
        }

        @Deprecated
        public Builder setHint(Collection<String> hints) {
            return this;
        }

        public static Builder withHint(String hint) {
            return Builder.of().appendHint(hint);
        }

        @Deprecated
        public Builder appendHint(String... hints) {
            return this;
        }

        public static Builder of(String... arg) {
            return new Builder(arg);
        }
    }
}
