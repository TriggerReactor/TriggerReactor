package js;

import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.AbstractExecutorManager.JSExecutor;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import js.components.DaggerExecutorTestComponent;
import js.components.ExecutorTestComponent;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ExecutorTest extends JsTest {
    private final AbstractExecutorManager manager;
    private final JSExecutor executor;
    public ExecutorTest(ScriptEngine engine, String name, String... directories) throws ScriptException, IOException {
        super(engine, name, "Executor", directories);
        manager = component.executorManager();
        executor = manager.new JSExecutor(name, engine, stream);
    }

    @Override
    public int getOverload(Object... args) {
        return executor.validate(args).getOverload();
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);
        executor.execute(Timings.LIMBO, varMap, null, args);
        return null;
    }
    private static final ExecutorTestComponent component = DaggerExecutorTestComponent.create();
    public static final Map<String, Boolean> coverage = new TreeMap<>();
}