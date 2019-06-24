package io.github.wysohn.triggerreactor.sponge.tools;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class TextUtil {
    public static Text colorStringToText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }
}
