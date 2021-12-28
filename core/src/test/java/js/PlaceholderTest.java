package js;

import io.github.wysohn.triggerreactor.core.manager.PlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.PlaceholderManager.JSPlaceholder;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import js.components.DaggerPlaceholderTestComponent;
import js.components.DaggerScriptEngineComponent;
import js.components.PlaceholderTestComponent;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class PlaceholderTest extends JsTest {
    private final PlaceholderManager manager = component.manager();
    private final JSPlaceholder placeholder;

    public PlaceholderTest(InterpreterLocalContext localContext, String name, String... directories) throws
            ScriptException, IOException {
        super(localContext, name, "Placeholder", directories);
        placeholder = manager.new JSPlaceholder(name, stream);
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);

        return placeholder.parse(Timings.LIMBO, localContext, localContext.getVars(), null, args);
    }

    @Override
    public int getOverload(Object... args) {
        return placeholder.validate(args).getOverload();
    }
    private static final PlaceholderTestComponent component = DaggerPlaceholderTestComponent.builder()
            .scriptEngineComponent(DaggerScriptEngineComponent.builder().initializer(new HashSet<>()).build())
            .build();
    public static final Map<String, Boolean> coverage = new TreeMap<>();
}