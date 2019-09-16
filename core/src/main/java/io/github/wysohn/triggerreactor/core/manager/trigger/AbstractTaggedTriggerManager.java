package io.github.wysohn.triggerreactor.core.manager.trigger;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;
import io.github.wysohn.triggerreactor.core.script.wrapper.SelfReference;

import java.io.File;

public abstract class AbstractTaggedTriggerManager extends AbstractTriggerManager {
    public AbstractTaggedTriggerManager(TriggerReactor plugin, SelfReference ref, File tirggerFolder) {
        super(plugin, ref, tirggerFolder);
    }

    /**
     * Extract prefix part of the trigger name. '-' sign will work as deliminator.
     * If there is no deliminator found, the 0th value of returned array will be null.
     * If there are more than one deliminator in the name, only the first String right before the first deliminator
     * encountered will be extracted, and the rest will be treated as one name.
     * For example, "some-name" would yield ["some", "name"], "some-name-bah" would yield ["some", "name-bah"],
     * and "something" would yield [null, "something].
     * @param rawTriggerName the raw trigger name which possibly contains the prefix. Should not be null.
     * @return the array containing split name. 0th value is prefix, and 1st value is the rest of the name. However,
     * 0th value will be null if no deliminator exist in the rawTriggerName.
     */
    protected static String[] extractPrefix(String rawTriggerName) {
        String[] split = rawTriggerName.split("-", 2);
        if(split.length < 2)
            return new String[]{null, rawTriggerName};

        return split;
    }

//    public static void main(String[] ar){
//        System.out.println(Arrays.toString(extractPrefix("some-name")));
//        System.out.println(Arrays.toString(extractPrefix("some-name-bah")));
//        System.out.println(Arrays.toString(extractPrefix("something")));
//    }
}
