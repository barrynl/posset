/**
 * 
 */
package nl.barry.posset;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.barry.posset.debug.Printer;
import nl.barry.posset.debug.TableOutputStream;
import nl.barry.posset.runtime.Element;
import syntax.SyntaxException;

/**
 * @author Barry
 * 
 */
@RunWith(Parameterized.class)
public class InterpreterBugTest {

	private static final Logger LOG = LoggerFactory.getLogger(InterpreterBugTest.class);

	private String resource;
	private int nrOfPossies;

	public InterpreterBugTest(String aResource, int aNrOfPossies) {
		resource = aResource;
		nrOfPossies = aNrOfPossies;
	}

	@Parameters
	public static Collection<?> data() {
		return Arrays.asList(new Object[][] {

				// @formatter:off
						{ "/DerivedPossetFourParent.pos", 1 },
						{ "/restriction.pos", 20 },
						{ "/IsZeroBoolean.pos", 10 },
						{ "/bug/NodeNameCausesInfiniteLoop.pos", 190 },
						{ "/bug/FluctuatingNumberOfPossiesReturned.pos", 190 },
						{ "/bug/OverlappingVariables.pos", 0},

						// prime posset: no restrictions
						{ "/PrimePossetSingleParentNoRestriction.pos", 2 },
						{ "/PrimePossetTwoParentNoRestriction.pos", 2 },
						{ "/PrimePossetTwoParentNoRestriction2.pos", 2 },
						{ "/PrimePossetThreeParentNoRestriction.pos", 2 },
						{ "/PrimePossetFourParentNoRestriction.pos", 2 },
						{ "/PrimePossetMultiParentNoRestriction.pos", 2 },

						// prime posset: with restrictions
						{ "/PrimePossetSingleParentSingleRestriction.pos", 1 },
						{ "/PrimePossetMultiParentSingleRestriction.pos", 1 },
						{ "/PrimePossetMultiParentSingleRestriction2.pos", 1 },
						{ "/PrimePossetMultiParentSingleRestriction3.pos", 1 },

						// derived posset: no restrictions
						{ "/DerivedPossetMultiParentNoRestriction.pos", 3 },
						{ "/DerivedPossetSingleParentSinglePartialRestriction.pos",2 },
						{ "/DerivedPossetSingleParentNoRestriction.pos", 1 },
						
						// power posset
						{ "/AggregationCollapsedVariablesWithin.pos", 16 },
						{ "/AggregationWithoutCollapsedVariables.pos", 1024 },
						{ "/AggregationWithLimitedPowerOfPosset.pos", 16 },
						{ "/TwoPowerPossetsCollapsing.pos", 2 }
						
						// @formatter:on
		});
	}

	@Test
	public void testInterpreter()
			throws Exception {
		Interpreter i = null;

		// read inputstream
		InputStream is = this.getClass().getResourceAsStream(resource);

		i = new Interpreter();
		i.loadSourceFile(is);
		i.prepareExecution();

		List<Element> possies = i.getPossies();

		TableOutputStream t = new TableOutputStream();
		boolean header = true;
		for (Element p : possies) {
			Printer.possyPrinter(p, header, System.out);
			Printer.possyPrinter(p, header, new PrintStream(t));
			header = false;
		}

		LOG.info("Number of possies: {}", possies.size());
		// analyseTable(t.getTable());
		Assert.assertEquals(this.nrOfPossies, possies.size());
	}

}
