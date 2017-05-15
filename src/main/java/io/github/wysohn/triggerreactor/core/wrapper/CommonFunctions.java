package io.github.wysohn.triggerreactor.core.wrapper;

import java.util.Random;

public class CommonFunctions {
    private static final Random rand = new Random();

    public int random(int end){
        return rand.nextInt(end);
    }

    public int random(int start, int end){
        return start + rand.nextInt(end - start);
    }
}
