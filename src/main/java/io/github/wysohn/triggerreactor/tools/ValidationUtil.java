package io.github.wysohn.triggerreactor.tools;

public class ValidationUtil {
    public static <T> void notNull(T obj){
        if(obj == null)
            throw new RuntimeException("Value cannot be null.");
    }
}
