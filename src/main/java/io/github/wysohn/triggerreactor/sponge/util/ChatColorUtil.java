package io.github.wysohn.triggerreactor.sponge.util;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public class ChatColorUtil {
    @SuppressWarnings("serial")
    private static final Map<Character, TextColor> colorMap = new HashMap<Character, TextColor>(){{
        put('0', TextColors.BLACK);
        put('1', TextColors.DARK_BLUE);
        put('2', TextColors.DARK_GREEN);
        put('3', TextColors.DARK_AQUA);
        put('4', TextColors.DARK_RED);
        put('5', TextColors.DARK_PURPLE);
        put('6', TextColors.GOLD);
        put('7', TextColors.GRAY);
        put('8', TextColors.DARK_GRAY);
        //put('9', TextColors.INDIGO);
        put('a', TextColors.GREEN);
        put('b', TextColors.AQUA);
        put('c', TextColors.RED);
        put('d', TextColors.LIGHT_PURPLE);
        put('e', TextColors.YELLOW);
        put('f', TextColors.WHITE);
    }};

    /**
     * Translate traditional color code alternative '&.'
     * @param original the original string contains '&' codes
     * @return translated message
     */
    public static Text translateColorcodes(String original){
        Builder builder = Text.builder();

        TextColor color = TextColors.NONE;
        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < original.length(); i++){
            if(original.charAt(i) == '&'
                    && i + 1 < original.length()
                    && colorMap.containsKey(original.charAt(i + 1))){
                //clear buffer
                builder.append(Text.builder(buffer.toString()).color(color).build());
                buffer = new StringBuilder();

                //set new color
                color = colorMap.get(original.charAt(i + 1));
                i++; //we read two characters
            } else {
                buffer.append(original.charAt(i));
            }
        }

        //final string
        builder.append(Text.builder(buffer.toString()).color(color).build());

        return builder.build();
    }
}
