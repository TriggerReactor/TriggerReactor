package js;

import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager.JSExecutor;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;

public class ExecutorTest extends JsTest {
    private final JSExecutor executor;

    public ExecutorTest(ScriptEngine engine, String name, String... directories) throws ScriptException, IOException {
        super(engine, name, "Executor", directories);
        executor = new JSExecutor(name, engine, stream);
    }

    @Override
    public Object test() throws Exception {
        executor.execute(Timings.LIMBO, true, varMap, null, args);
        return null;
    }

    @Override
    public int getOverload(Object... args) {
        return executor.validate(args).getOverload();
    }
}