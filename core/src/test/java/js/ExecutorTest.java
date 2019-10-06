package js;

import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager.JSExecutor;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ExecutorTest extends JsTest{
	//I had to include this to get around access restrictions.  Plz don't kill me wysohn
	private class JSExecutorAlias extends JSExecutor {
		public JSExecutorAlias(String executorName, ScriptEngine engine, InputStream stream)
				throws ScriptException, IOException {
			super(executorName, engine, stream);
		}
		
		@Override
		public Integer execute(Timings.Timing timing, boolean sync, Map<String, Object> variables, Object e,
                Object... args) throws Exception {
			return super.execute(null, sync, variables, e, args);
		}
	}
	
	private final JSExecutorAlias executor;
	
	public ExecutorTest(ScriptEngine engine, String name, String... directories) throws ScriptException, IOException {
		super(engine, name, "Executor", directories);
		executor = new JSExecutorAlias(name, engine, stream);
	}

	@Override
    public Object test() throws Exception {
        executor.execute(Timings.LIMBO, true, varMap, null, args);
        return null;
    }
}