package io.github.wysohn.triggerreactor.tools.trie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringListTrie extends Trie {
    public StringListTrie() {
    }

    public StringListTrie(Iterable<String> iterable) {
        iterable.forEach(this::insert);
    }

    private void addAll(Collection<String> collection, TrieNode node, String current) {
        if (node == null)
            return;

        if (node.isWord())
            collection.add(current);

        node.getChildren().forEach((c, child_node) -> addAll(collection, child_node, current + c));
    }

    public List<String> getAllStartsWith(String str) {
        List<String> list = new ArrayList<>();
        if (str == null || str.length() < 1)
            return list;

        TrieNode node = findNode(str);
        addAll(list, node, str);

        return list;
    }
}
