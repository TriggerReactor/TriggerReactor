package js;

import io.github.wysohn.triggerreactor.core.script.interpreter.InterpreterLocalContext;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class JsTest {
    protected final String name;
    protected final InputStream stream;
    protected InterpreterLocalContext localContext;
    protected Object[] args;

    /**
     * @param name             the name of the js file being tested (last item in file path)
     * @param otherDirectories list of directories to go through to reach the file, such as PLAYER for PLAYER/SETFLYMODE
     * @throws FileNotFoundException
     */
    protected JsTest(InterpreterLocalContext localContext,
                     String name,
                     String firstDirectory,
                     String... otherDirectories) throws FileNotFoundException {
        this.name = name;
        this.localContext = localContext;

        StringBuilder builder = new StringBuilder();

        builder.append(firstDirectory);
        builder.append('/');
        for (String dir : otherDirectories) {
            builder.append(dir);
            builder.append('/');
        }
        builder.append(name + ".js");
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(builder.toString());
        if (stream == null) {
            throw new FileNotFoundException("could not find file " + builder.toString());
        }
        this.stream = stream;
        this.args = new Object[]{};
    }

    public abstract Object test() throws Exception;

    public JsTest addVariable(String name, Object value) {
        localContext.getVars().put(name, value);
        return this;
    }

    public JsTest assertInvalid(Object... args) {
        assertFalse(isValid(args));
        return this;
    }

    public boolean isValid(Object... args) {
        return getOverload(args) != -1;
    }

    public abstract int getOverload(Object... args);

    public JsTest assertValid(Object... args) {
        assertTrue(isValid(args));
        return this;
    }

    public JsTest withArgs(Object... args) {
        this.args = args;
        return this;
    }
}
