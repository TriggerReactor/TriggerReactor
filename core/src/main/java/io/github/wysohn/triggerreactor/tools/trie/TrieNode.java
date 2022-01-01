package io.github.wysohn.triggerreactor.tools.trie;

import java.util.HashMap;

public class TrieNode {
    private final HashMap<Character, TrieNode> children = new HashMap<>();
    private boolean isWord;

    public HashMap<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isWord() {
        return isWord;
    }

    public void setWord(boolean word) {
        isWord = word;
    }
}
