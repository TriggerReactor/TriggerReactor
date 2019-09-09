package io.github.wysohn.triggerreactor.tools;

public class StringUtils {
    public static String spaces(int n){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < n; i++)
            builder.append(' ');
        return builder.toString();
    }
}
