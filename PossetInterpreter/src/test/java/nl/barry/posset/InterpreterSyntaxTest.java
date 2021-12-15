/**
 * 
 */
package nl.barry.posset;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import nl.barry.posset.runtime.Element;
import syntax.ChildDoesNotExistException;
import syntax.IncompatibleRestrictionValuesException;
import syntax.IncompatibleVariableTargetsException;
import syntax.InvalidRestrictionVariableLocationException;
import syntax.NoPrimeNumberException;
import syntax.PossetNotFoundException;
import syntax.SyntaxException;

/**
 * @author Barry
 * 
 */
@RunWith(Parameterized.class)
public class InterpreterSyntaxTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	public String resource;
	public Class<Throwable> expectedException;

	public InterpreterSyntaxTest(String aResource, Class<Throwable> anExpectedException) {
		resource = aResource;
		expectedException = anExpectedException;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				//@formatter:off
				{ "/syntax-errors/non-existing-childname.pos",	ChildDoesNotExistException.class },
				{ "/syntax-errors/different-number-of-restriction-values-compared-to-restriction-headers.pos", IncompatibleRestrictionValuesException.class },
				{ "/syntax-errors/incompatible-variable-targets.pos", IncompatibleVariableTargetsException.class },
				{ "/syntax-errors/same-name-for-sub-posset.pos", Exception.class },
				{ "/syntax-errors/non-existing-posset.pos", PossetNotFoundException.class },
				{ "/syntax-errors/invalid-restriction-variable-location.pos", InvalidRestrictionVariableLocationException.class },
				{ "/syntax-errors/not-a-prime-number.pos", NoPrimeNumberException.class },
				/*{ "/syntax-errors/variable-deathlock.pos", VariableDeathlockException.class }*/ });
				//@formatter:on
	}

	@Test
	public void testSyntaxErrors() throws Exception, FileNotFoundException, ParseException,
			nl.barry.posset.ast.ParseException, SyntaxException {
		Interpreter i;

		// read inputstream
		InputStream is = this.getClass().getResourceAsStream(resource);

		i = new Interpreter();
		this.exception.expect(this.expectedException);
		i.loadSourceFile(is);
		i.prepareExecution();
		List<Element> possies = i.getPossies();
	}
}
