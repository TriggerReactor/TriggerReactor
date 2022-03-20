package js;

import io.github.wysohn.triggerreactor.core.manager.PlaceholderManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.Mockito.mock;

public class PlaceholderTest extends JsTest {
    private final InterpreterLocalContext localContext;
    private final PlaceholderManager.JSPlaceholder placeholder;

    public PlaceholderTest(InterpreterLocalContext localContext,
                           String name,
                           String... directories) throws ScriptException, IOException {
        super(name, "Placeholder", directories);
        this.localContext = localContext;

        PlaceholderManager manager = mock(PlaceholderManager.class);
        placeholder = manager.new JSPlaceholder(name, stream);
    }

    @Override
    public int getOverload(Object... args) {
        return placeholder.validate(args).getOverload();
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);

        return placeholder.parse(Timings.LIMBO, localContext, varMap, args);
    }

    public static final Map<String, Boolean> coverage = new TreeMap<>();
}