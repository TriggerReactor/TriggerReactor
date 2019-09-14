package io.github.wysohn.triggerreactor.tools.timings;

import org.junit.Assert;
import org.junit.Test;

public class TimingsTest {

    @Test
    public void testTiming() {
        Timings.Timing timingName = Timings.getTiming("my.timing.name");
        Timings.Timing timingTiming = Timings.getTiming("my.timing");
        Timings.Timing timingMy = Timings.getTiming("my");
        Timings.Timing root = Timings.getTiming(null);

        Assert.assertNotNull(timingName);
        Assert.assertNotNull(timingTiming);
        Assert.assertNotNull(timingMy);
        Assert.assertNotNull(root);

        Assert.assertEquals(timingName, Timings.getTiming("my.timing.name"));
        Assert.assertEquals(timingTiming, Timings.getTiming("my.timing"));
        Assert.assertEquals(timingMy, Timings.getTiming("my"));
        Assert.assertEquals(root, Timings.getTiming(null));
    }

    @Test
    public void testExampleTiming() throws Exception{
        Timings.on = true;

        Timings.Timing timingExMessage = Timings.getTiming("CommandTrigger.myCmd.Executors.#MESSAGE");
        Timings.Timing timingExTP = Timings.getTiming("CommandTrigger.myCmd.Executors.#TP");

        Timings.Timing timingPhPlayerName = Timings.getTiming("CommandTrigger.myCmd.Placeholders.$playername");
        Timings.Timing timingPhRandom = Timings.getTiming("CommandTrigger.myCmd.Placeholders.$random");

        Timings.Timing timingInterpret = Timings.getTiming("CommandTrigger.myCmd.Interpret");

        try(Timings.Timing t = timingExMessage.begin(true)){
            Thread.sleep(2L);
        }
        try(Timings.Timing t = timingExTP.begin()){
            Thread.sleep(5L);
        }

        try(Timings.Timing t = timingPhPlayerName.begin()){
            Thread.sleep(100L);
        }
        try(Timings.Timing t = timingPhRandom.begin()){
            Thread.sleep(10L);
        }

        try(Timings.Timing t = timingInterpret.begin()){
            Thread.sleep(1000L);
        }

        Timings.Timing parent = Timings.getTiming("CommandTrigger");
        Timings.Timing timingExMessage2 = parent.getTiming("myCmd2.Executors.#MESSAGE");

        try(Timings.Timing t = timingExMessage2.begin()){
            Thread.sleep(1L);
        }

        //Timings.print(Timings.getTiming(null), System.out);
    }
}