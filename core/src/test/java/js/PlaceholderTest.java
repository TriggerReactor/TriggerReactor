package js;

import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager.JSPlaceholder;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;

public class PlaceholderTest extends JsTest{
	private final JSPlaceholder placeholder;
	
	public PlaceholderTest(ScriptEngine engine, String name, String... directories) throws ScriptException, IOException {
		super(engine, name, "Placeholder", directories);
		placeholder = new JSPlaceholder(name, engine, stream);
	}

	@Override
    public Object test() throws Exception {
        return placeholder.parse(Timings.LIMBO, true, varMap, null, args);
    }

	@Override
	public int getOverload(Object... args) {
		return placeholder.validate(args).getOverload();
	}
}