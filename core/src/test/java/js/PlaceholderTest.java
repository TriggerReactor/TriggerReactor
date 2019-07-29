package js;

import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import io.github.wysohn.triggerreactor.core.manager.AbstractPlaceholderManager.JSPlaceholder;

public class PlaceholderTest extends JsTest{
	private final JSPlaceholder placeholder;
	
	public PlaceholderTest(ScriptEngine engine, String name, String... directories) throws ScriptException, IOException {
		super(engine, name, "Placeholder", directories);
		placeholder = new JSPlaceholder(name, engine, stream);
	}

	@Override
    public Object test() throws Exception {
        return placeholder.parse(true, varMap, null, args);
    }
}