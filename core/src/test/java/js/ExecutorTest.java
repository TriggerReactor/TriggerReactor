package js;

import io.github.wysohn.triggerreactor.core.manager.ExecutorManager;
import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;
import io.github.wysohn.triggerreactor.tools.timings.Timings;

import java.util.Map;
import java.util.TreeMap;

import static org.mockito.Mockito.mock;

public class ExecutorTest extends JsTest {
    private final InterpreterLocalContext localContext;
    private final ExecutorManager.JSExecutor executor;

    public ExecutorTest(InterpreterLocalContext localContext, String name, String... directories) throws Exception {
        super(name, "Executor", directories);
        this.localContext = localContext;

        ExecutorManager mockManager = mock(ExecutorManager.class);
        executor = mockManager.new JSExecutor(name, stream);
    }

    @Override
    public int getOverload(Object... args) {
        return executor.validate(args).getOverload();
    }

    @Override
    public Object test() throws Exception {
        coverage.put(this.name, true);
        executor.execute(Timings.LIMBO, localContext, varMap, args);
        return null;
    }

    public static final Map<String, Boolean> coverage = new TreeMap<>();
}