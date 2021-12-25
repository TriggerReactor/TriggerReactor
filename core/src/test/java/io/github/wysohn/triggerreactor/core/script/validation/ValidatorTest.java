package io.github.wysohn.triggerreactor.core.script.validation;

import org.junit.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ValidatorTest {
    @Test
    public void testJSR() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
        if (engine == null) return;
        System.out.println("GraalJS available");

        engine.eval("validation =  {\n" + "\t\"overloads\" : [\n" + "\t\t[],\n" + "\t\t[{\"type\": \"string\", \"name\": \"effect type\"}]\n" + "\t]\n" + "}\n" + "" + "function CLEARPOTION(args){\n" + "\tif(player === null)\n" + "\t\treturn null;\n" + "\n" + "\tif(overload === 0){\n" + "\t\tvar activeEffects = player.getActivePotionEffects();\n" + "\t\tfor(var iter = activeEffects.iterator(); iter.hasNext();){\n" + "\t\t\tvar type = iter.next().getType();\n" + "\t\t\tplayer.removePotionEffect(type);\n" + "\t\t}\n" + "\t}else{\n" + "\t\tvar typeName = args[0].toUpperCase();\n" + "\t\tvar PotionEffectType = Java.type('org.bukkit.potion.PotionEffectType');\n" + "\t\tvar type = PotionEffectType.getByName(typeName);\n" + "\t\t\n" + "\t\tif(type == null)\n" + "\t\t\tthrow new Error(\"Invalid PotionEffectType named \"+typeName);\n" + "\t\t\t\n" + "\t\tplayer.removePotionEffect(type);\n" + "\t}\n" + "}");

        ScriptContext context = engine.getContext();
        Map<String, Object> attribute = (Map<String, Object>) context.getAttribute("validation");
        System.out.println(attribute);
        Validator validator = Validator.from(attribute);
        assertEquals(0, validator.validate().getOverload());
        assertEquals(1, validator.validate("MyPotionType").getOverload());
    }

    @Test
    public void testJSRNashorn() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        if (engine == null) return;
        System.out.println("Nashorn engine available");

        engine.eval("validation =  {\n" + "\t\"overloads\" : [\n" + "\t\t[],\n" + "\t\t[{\"type\": \"string\", \"name\": \"effect type\"}]\n" + "\t]\n" + "}\n" + "" + "function CLEARPOTION(args){\n" + "\tif(player === null)\n" + "\t\treturn null;\n" + "\n" + "\tif(overload === 0){\n" + "\t\tvar activeEffects = player.getActivePotionEffects();\n" + "\t\tfor(var iter = activeEffects.iterator(); iter.hasNext();){\n" + "\t\t\tvar type = iter.next().getType();\n" + "\t\t\tplayer.removePotionEffect(type);\n" + "\t\t}\n" + "\t}else{\n" + "\t\tvar typeName = args[0].toUpperCase();\n" + "\t\tvar PotionEffectType = Java.type('org.bukkit.potion.PotionEffectType');\n" + "\t\tvar type = PotionEffectType.getByName(typeName);\n" + "\t\t\n" + "\t\tif(type == null)\n" + "\t\t\tthrow new Error(\"Invalid PotionEffectType named \"+typeName);\n" + "\t\t\t\n" + "\t\tplayer.removePotionEffect(type);\n" + "\t}\n" + "}");

        ScriptContext context = engine.getContext();
        Map<String, Object> attribute = (Map<String, Object>) context.getAttribute("validation");
        System.out.println(attribute);
        Validator validator = Validator.from(attribute);
        assertEquals(0, validator.validate().getOverload());
        assertEquals(1, validator.validate("MyPotionType").getOverload());
    }
}