/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    private static Pattern pattern = Pattern.compile("(\\d+)(h|m|s)");

    /**
     * convert time format into milliseconds
     * @param str the format string
     * @return time in milliseconds
     */
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

    /**
     * Convert interval into formatted
     * @param interval
     * @return
     */
    public static String milliSecondsToString(long interval){
        int r = 0;

        int hour = (int)interval / (60 * 60 * 1000);
        r = (int)interval % (60 * 60 * 1000);

        int minute = r / (60 * 1000);
        r = r % (60 * 1000);

        int second = r / (1000);

        return hour+"h "+minute+"m "+second+"s";
    }

    public static void main(String[] ar){
        System.out.println(parseTime("12h33m50s"));
    }
}
