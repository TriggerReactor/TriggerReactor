package io.github.wysohn.triggerreactor.core.manager.trigger.command;

import io.github.wysohn.triggerreactor.tools.trie.StringListTrie;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Simple tab completer with internal caching enabled.
 */
public class DynamicTabCompleter implements ITabCompleter {
    private final String identifier;
    private final Supplier<List<String>> candidateSupplier;
    private final long updatePeriod;

    private long lastUpdate = -1L;
    private StringListTrie trie = null;

    /**
     * @param identifier        identifier (such as $playerlist)
     * @param candidateSupplier supplier
     * @param updatePeriod      update period. It can be 0 if you want to disable caching.
     */
    public DynamicTabCompleter(String identifier, Supplier<List<String>> candidateSupplier, long updatePeriod) {
        this.identifier = identifier;
        this.candidateSupplier = candidateSupplier;
        this.updatePeriod = updatePeriod;
    }

    /**
     * Tab completer with caching enabled with period of 100 milliseconds
     *
     * @param identifier        identifier (such as $playerlist)
     * @param candidateSupplier supplier
     */
    public DynamicTabCompleter(String identifier, Supplier<List<String>> candidateSupplier) {
        this(identifier, candidateSupplier, 100L);
    }

    @Override
    public List<String> getCandidates(String part) {
        if (System.currentTimeMillis() > lastUpdate + updatePeriod) {
            List<String> candidates = new LinkedList<>(candidateSupplier.get());
            trie = new StringListTrie(candidates);
            lastUpdate = System.currentTimeMillis();
        }

        return trie.getAllStartsWith(part);
    }

    @Override
    public String asConfigString() {
        return identifier;
    }

    public static class Builder {
        private final String identifier;
        private Supplier<List<String>> supplier;
        private long updatePeriod = 0L;

        private boolean build = false;

        private Builder(String identifier, Supplier<List<String>> supplier) {
            this.identifier = identifier;
            this.supplier = supplier;
        }

        /**
         * @param cachePeriod set it 0 to disable (disabled by default)
         */
        public Builder caching(long cachePeriod) {
            this.updatePeriod = cachePeriod;
            return this;
        }

        public DynamicTabCompleter[] buildAsArray() {
            return new DynamicTabCompleter[]{build()};
        }

        public DynamicTabCompleter build() {
            // we don't want the candidates and hints List references to be
            // shared with multiple instances.
            if (build)
                throw new RuntimeException("Do not use the same builder twice.");
            build = true;

            return new DynamicTabCompleter(identifier, supplier, updatePeriod);
        }

        public static Builder of(String identifier, Supplier<List<String>> supplier) {
            return new Builder(identifier, supplier);
        }
    }
}
