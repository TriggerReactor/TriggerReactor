package js;

import io.github.wysohn.triggerreactor.core.manager.ExecutorManager;
import io.github.wysohn.triggerreactor.core.manager.ExecutorManager.JSExecutor;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.tools.timings.Timings;
import js.components.DaggerExecutorTestComponent;
import js.components.DaggerScriptEngineComponent;
import js.components.ExecutorTestComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class ExecutorTest extends JsTest {
    private final ExecutorManager manager;
    private final JSExecutor executor;

    public ExecutorTest(InterpreterLocalContext localContext, String name, String... directories) throws Exception {
        super(localContext, name, "Executor", directories);
        manager = component.executorManager();
        manager.onEnable();

        executor = manager.new JSExecutor(name, stream);
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);
        executor.execute(Timings.LIMBO, localContext, localContext.getVars(), args);
        return null;
    }

    @Override
    public int getOverload(Object... args) {
        return executor.validate(args).getOverload();
    }
    private static final ExecutorTestComponent component = DaggerExecutorTestComponent.builder()
            .engineComponent(DaggerScriptEngineComponent.builder().initializer(new HashSet<>()).build())
            .build();
    public static final Map<String, Boolean> coverage = new TreeMap<>();
}