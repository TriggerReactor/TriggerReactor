package io.github.wysohn.triggerreactor.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static Pattern pattern = Pattern.compile("(\\d+)(h|m|s)");
    public static long parseTime(String str){
        long sum = 0;

        Matcher matcher = pattern.matcher(str);
        while(matcher.find()){
            switch(matcher.group(2)){
            case "h":
                sum += Long.parseLong(matcher.group(1)) * 60 * 60 * 1000;
                break;
            case "m":
                sum += Long.parseLong(matcher.group(1)) * 60 * 1000;
                break;
            case "s":
                sum += Long.parseLong(matcher.group(1)) * 1000;
                break;
            }
        }

        return sum;
    }

    public static void main(String[] ar){
        System.out.println(parseTime("12h33m50s"));
    }
}
