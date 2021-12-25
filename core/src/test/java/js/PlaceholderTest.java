package js;

import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager.JSPlaceholder;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import js.components.DaggerPlaceholderTestComponent;
import js.components.PlaceholderTestComponent;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class PlaceholderTest extends JsTest {
    private final AbstractPlaceholderManager manager = component.manager();
    private final JSPlaceholder placeholder;
    public PlaceholderTest(ScriptEngine engine,
                           String name,
                           String... directories) throws ScriptException, IOException {
        super(engine, name, "Placeholder", directories);
        placeholder = manager.new JSPlaceholder(name, engine, stream);
    }

    @Override
    public int getOverload(Object... args) {
        return placeholder.validate(args).getOverload();
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);

        return placeholder.parse(Timings.LIMBO, null, varMap, args);
    }
    private static final PlaceholderTestComponent component = DaggerPlaceholderTestComponent.create();
    public static final Map<String, Boolean> coverage = new TreeMap<>();
}