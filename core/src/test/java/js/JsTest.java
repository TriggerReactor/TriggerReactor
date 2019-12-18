package js;

import javax.script.ScriptEngine;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class JsTest {
    protected final InputStream stream;
    protected final ScriptEngine engine;
    protected Map<String, Object> varMap;
    protected Object[] args;

    /**
     *
     * 
     * @param engine      the script engine to use
     * @param name        the name of the js file being tested (last item in file path)
     * @param otherDirectories list of directories to go through to reach the file, such as PLAYER for PLAYER/SETFLYMODE
     * @throws FileNotFoundException 
     */
    protected JsTest(ScriptEngine engine, String name, String firstDirectory, String... otherDirectories) throws FileNotFoundException {
    	StringBuilder builder = new StringBuilder();
    	
    	
    	builder.append(firstDirectory);
		builder.append('/');
    	for (String dir : otherDirectories) {
    		builder.append(dir);
    		builder.append('/');
    	}
    	builder.append(name + ".js");
        this.engine = engine;
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(builder.toString());
        if (stream == null) {
        	throw new FileNotFoundException("could not find file " + builder.toString());
        }
        this.stream = stream;
        this.varMap = new HashMap<>();
        this.args = new Object[] {};
    }
    
    public JsTest addVariable(String name, Object value) {
    	varMap.put(name, value);
        return this;
    }

    public JsTest withArgs(Object... args) {
        this.args = args;
        return this;
    }

    public abstract Object test() throws Exception;

    public abstract int getOverload(Object... args);

    public boolean isValid(Object... args) {
        return getOverload(args) != -1;
    }

    public JsTest assertValid(Object... args) {
        assertTrue(isValid(args));
        return this;
    }

    public JsTest assertInvalid(Object... args) {
        assertFalse(isValid(args));
        return this;
    }
}
