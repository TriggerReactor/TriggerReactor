package js;

import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager.JSPlaceholder;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.Mockito.mock;

public class PlaceholderTest extends JsTest {
    public static final Map<String, Boolean> coverage = new TreeMap<>();
    private final AbstractPlaceholderManager manager = mock(AbstractPlaceholderManager.class);
    private final JSPlaceholder placeholder;

    public PlaceholderTest(ScriptEngine engine, String name, String... directories) throws ScriptException, IOException {
        super(engine, name, "Placeholder", directories);
        placeholder = manager.new JSPlaceholder(name, engine, stream);
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);

        return placeholder.parse(Timings.LIMBO, null, varMap, args);
    }

    @Override
    public int getOverload(Object... args) {
        return placeholder.validate(args).getOverload();
    }
}