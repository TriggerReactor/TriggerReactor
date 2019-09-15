package io.github.wysohn.triggerreactor.core.script.warning;

import org.junit.Test;

import io.github.wysohn.triggerreactor.core.script.lexer.Lexer;
import io.github.wysohn.triggerreactor.core.script.lexer.LexerException;
import io.github.wysohn.triggerreactor.core.script.parser.Parser;
import io.github.wysohn.triggerreactor.core.script.parser.ParserException;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class TestWarnings {
	
	public static List<Warning> warningsFrom(String script) throws IOException, LexerException, ParserException {
		Parser p = new Parser(new Lexer(script, Charset.forName("UTF-8")));
		p.parse(true);
		return p.getWarnings();
	}
	
	/*@Test
	public void testWarningEquals() {
		Warning w1 = new StringInterpolationWarning(0, "merp");
		Warning w2 = new DeprecationWarning(0, "merp", "derp");
		
		Warning w3 = new DeprecationWarning(0, "merp", "merp");
		Warning w4 = new DeprecationWarning(0, "merp", "merp");
		assertFalse(w1.equals(w2));
		assertFalse(w2.equals(w4));
		
		assertTrue(w3.equals(w4));
	}*/
	
	@Test
	public void testInterpolationWarning() throws Exception {
		//#MESSAGE "pay me $3"
		String escaped = "#MESSAGE \"pay me \\$3\"";
		String notEscaped = "#MESSAGE \"pay me $3\"";
		String notEscaped2 = "#MESSAGE \"pay me \\\\$3\"";
		
		assertEquals(0, warningsFrom(escaped).size());
		
		Warning expected = new StringInterpolationWarning(1, notEscaped);
		assertTrue(expected.equals(warningsFrom(notEscaped).get(0)));
		
		expected = new StringInterpolationWarning(1, notEscaped2);
		assertTrue(expected.equals(warningsFrom(notEscaped2).get(0)));
	}
}
