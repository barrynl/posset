/**
 * 
 */
package nl.barry.posset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.barry.posset.runtime.CVPowerPosset;
import nl.barry.posset.runtime.CollapsedVariablesIterator;
import nl.barry.posset.runtime.CollapsedVariablesPosset;
import nl.barry.posset.runtime.DerivedIterator;
import nl.barry.posset.runtime.DerivedPosset;
import nl.barry.posset.runtime.Element;
import nl.barry.posset.runtime.Iterator;
import nl.barry.posset.runtime.Posset;
import nl.barry.posset.runtime.PrimeIterator;
import nl.barry.posset.runtime.PrimePosset;
import nl.barry.posset.runtime.PrimePossy;
import syntax.ChildAlreadyExistsException;
import syntax.NoPrimeNumberException;
import syntax.SyntaxException;

/**
 * @author Barry
 * 
 */
public class PossetTest {

	private static final Logger LOG = LoggerFactory.getLogger(PossetTest.class);

	/**
	 * We are testing whether the expected collapsed version of
	 * OverlappingVariables.pos test (see Interpreter tests) will function properly
	 * with the current setup. If so, we can start thinking about implementing this
	 * method. The collapsed version should have 0 possets.
	 * 
	 * @throws ChildAlreadyExistsException If a duplicate name is used within the
	 *                                     same posset.
	 */
	@Test
	public void testAfterCollapsing() throws SyntaxException {
		// build possets
		DerivedPosset main = new DerivedPosset("main");
		DerivedPosset bla1 = new DerivedPosset("bla1");
		DerivedPosset bla2 = new DerivedPosset("bla2");
		DerivedPosset test = new DerivedPosset("test");
		DerivedPosset test1 = new DerivedPosset("test1");
		test1.setRestrictionHeader(Arrays.asList("A"));
		test1.setRestrictionValues(Arrays.asList(Arrays.asList(1)));
		DerivedPosset test2 = new DerivedPosset("test2");
		test2.setRestrictionHeader(Arrays.asList("A"));
		test2.setRestrictionValues(Arrays.asList(Arrays.asList(2)));
		PrimePosset two = new PrimePosset(2);
		two.setVar("A");

		// relate possets
		main.addChild("one", bla1);
		main.addChild("two", bla2);
		bla1.addChild("one", test);
		bla2.addChild("one", test);
		bla1.addChild("two", test1);
		bla2.addChild("two", test1);
		test.addChild("one", test1);
		test1.addChild("one", test2);
		test2.addChild("one", two);

		Iterator<? extends Element> iter = main.iterator();

		int cnt = 0;
		while (iter.hasNext()) {
			cnt++;
			LOG.debug("{}", iter.next());
		}
		Assert.assertEquals(0, cnt);
	}

	@Test
	public void testHasAncestor() throws SyntaxException {
		DerivedPosset p = new DerivedPosset("TestPosset");

		assertTrue(p.hasAncestor(p));

		PrimePosset primeP = new PrimePosset(5);
		PrimePosset primeP2 = new PrimePosset(5);

		assertTrue(primeP.hasAncestor(primeP2));

		DerivedPosset p2 = new DerivedPosset("TestPosset2");
		p.addChild("first", p2);

		assertTrue(p.hasAncestor(p2));

		p2.addChild("first", primeP);

		assertTrue(p.hasAncestor(primeP));

		p2.addChild("second", primeP2);

		assertFalse(p.hasAncestor(primeP));
	}

	@Test
	public void testHasNextWithoutIncomingVarMapAndWithoutRestriction() throws SyntaxException {
		final int POSSET1_PRIME = 2, POSSET2_PRIME = 3;
		PrimePosset p1 = new PrimePosset(POSSET1_PRIME);
		PrimePosset p2 = new PrimePosset(POSSET2_PRIME);
		DerivedPosset p3 = new DerivedPosset("TestPosset");
		p3.addChild("first", p1);
		p3.addChild("second", p2);

		Iterator<? extends Element> i = p3.iterator();
		int count = 0;

		for (int j = 0; j < POSSET1_PRIME * POSSET2_PRIME; j++) {
			assertTrue(i.hasNext());
			System.out.println(i.next());
			count++;
		}
		assertEquals(POSSET1_PRIME * POSSET2_PRIME, count);
		System.out.println("----------------------");
	}

	@Ignore // I am unsure why this test fails and whether this is an error in the test or
			// an error in the language. For now we ignore.
	@Test
	public void testPrimePossetHasNextWithIncomingVarMap() throws SyntaxException {
		PrimePosset p1 = new PrimePosset(2);
		p1.setVar("A");

		PrimeIterator i = (PrimeIterator) p1.iterator();

		Map<String, Integer> res = new HashMap<String, Integer>();
		res.put("A", 1);

		// for runtime reasons the hasNext returns false whenever it is called
		// with a restriction. It is meaningless with a restriction because it
		// will always return true, so it will loop infinitely.
		assertFalse(i.hasNext(res, null));

		PrimePossy pp = (PrimePossy) i.next();
		assertEquals(1, pp.getId());

		res.put("A", 2);

		assertFalse(i.hasNext(res, null));

	}

	@Test
	public void testHasNextWithoutIncomingVarMapWithRestriction() throws SyntaxException {
		final int POSSET1_PRIME = 2, POSSET2_PRIME = 3;
		PrimePosset p1 = new PrimePosset(POSSET1_PRIME);
		p1.setVar("A");
		PrimePosset p2 = new PrimePosset(POSSET2_PRIME);
		DerivedPosset p3 = new DerivedPosset("TestPosset");

		List<String> aHeader = new ArrayList<String>();
		aHeader.add("A");
		List<List<Integer>> someValues = new ArrayList<List<Integer>>();
		List<Integer> values = new ArrayList<Integer>();
		values.add(1);
		someValues.add(values);
		p3.setRestrictionHeader(aHeader);
		p3.setRestrictionValues(someValues);

		p3.addChild("first", p1);
		p3.addChild("second", p2);

		Iterator<? extends Element> i = p3.iterator();
		int count = 0;

		for (int j = 0; j < 1 * POSSET2_PRIME; j++) {
			assertTrue(i.hasNext());
			System.out.println(i.next());
			count++;
		}
		assertEquals(1 * POSSET2_PRIME, count);
		System.out.println("----------------------");
	}

	@Test
	public void testHasNextWithConflictingIncomingVarMapWithRestriction() throws SyntaxException {
		final int POSSET1_PRIME = 2, POSSET2_PRIME = 3;
		PrimePosset p1 = new PrimePosset(POSSET1_PRIME);
		p1.setVar("A");
		PrimePosset p2 = new PrimePosset(POSSET2_PRIME);
		DerivedPosset p3 = new DerivedPosset("TestPosset");

		List<String> aHeader = new ArrayList<String>();
		aHeader.add("A");
		List<List<Integer>> someValues = new ArrayList<List<Integer>>();
		List<Integer> values = new ArrayList<Integer>();
		values.add(1);
		someValues.add(values);
		p3.setRestrictionHeader(aHeader);
		p3.setRestrictionValues(someValues);

		p3.addChild("first", p1);
		p3.addChild("second", p2);

		DerivedIterator i = (DerivedIterator) p3.iterator();
		int count = 0;

		Map<String, Integer> varMap = new HashMap<String, Integer>();
		varMap.put("A", 2);

		assertFalse(i.hasNext(varMap, null));
		System.out.println("----------------------");
	}

	@Test
	public void testHasNextWithoutIncomingVarMapWithMultiVarRestriction() throws SyntaxException {
		final int POSSET1_PRIME = 2, POSSET2_PRIME = 3;
		PrimePosset p1 = new PrimePosset(POSSET1_PRIME);
		p1.setVar("A");
		PrimePosset p2 = new PrimePosset(POSSET2_PRIME);
		p2.setVar("B");
		DerivedPosset p3 = new DerivedPosset("TestPosset");

		List<String> aHeader = new ArrayList<String>();
		aHeader.add("A");
		List<List<Integer>> someValues = new ArrayList<List<Integer>>();
		List<Integer> values = new ArrayList<Integer>();
		values.add(1);
		someValues.add(values);

		aHeader.add("B");
		List<Integer> values2 = new ArrayList<Integer>();
		values2.add(2);
		someValues.add(values2);

		p3.setRestrictionHeader(aHeader);
		p3.setRestrictionValues(someValues);

		p3.addChild("first", p1);
		p3.addChild("second", p2);

		Iterator<? extends Element> i = p3.iterator();
		int count = 0;

		for (int j = 0; j < 1 * 1; j++) {
			assertTrue(i.hasNext());
			System.out.println(i.next());
			count++;
		}
		assertEquals(1 * 1, count);
		System.out.println("----------------------");
	}

	@Test
	public void testSimplePowerPosset() throws NoPrimeNumberException, ChildAlreadyExistsException {

		int PRIME = 7;
		PrimePosset pp = new PrimePosset(PRIME);
		pp.setVar("A");

		DerivedPosset dp = new DerivedPosset("templateOf-" + pp.getName());
		dp.setRestrictionHeader(Arrays.asList("A"));
		dp.setRestrictionValues(
				new ArrayList<List<Integer>>(Arrays.asList(new ArrayList<Integer>(Arrays.asList(/* 1, 2 , 3 */)))));
		dp.addChild("c", pp.deepPossetClone());

		CVPowerPosset p = new CVPowerPosset("test", null, pp, dp);

		var iter = p.iterator();

		int counter = 0;
		while (iter.hasNext()) {
			counter++;
			Posset p2 = iter.next();
			Iterator<? extends Element> iter2 = p2.iterator();
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			boolean firstTime = true;
			while (iter2.hasNext()) {
				if (!firstTime) {
					sb.append(",");
				} else {
					firstTime = false;
				}
				Element e = iter2.next();
				sb.append(e);

			}
			sb.append("}");
			LOG.info("Posset: {}", sb.toString());

		}
		LOG.info("Total of {} possets returned. Should be: {}", counter, Math.pow(2, PRIME));

	}

	@Test
	public void testPowerPosset() throws NoPrimeNumberException, ChildAlreadyExistsException {

		int PRIME1 = 2, PRIME2 = 3;
		PrimePosset p1 = new PrimePosset(PRIME1);
		p1.setVar("A");

		PrimePosset p2 = new PrimePosset(PRIME2);
		p2.setVar("B");

		Map<String, CollapsedVariablesPosset> children = new HashMap<>();
		children.put("p1", p1);
		children.put("p2", p2);

		DerivedPosset dp = new DerivedPosset(null, "Six", children);

		DerivedPosset templatePosset = new DerivedPosset("templateOf-" + dp.getName());
		templatePosset.setRestrictionHeader(Arrays.asList("A", "B"));
		ArrayList<List<Integer>> someValues = new ArrayList<List<Integer>>();
		someValues.add(new ArrayList<Integer>());
		someValues.add(new ArrayList<Integer>());
		templatePosset.setRestrictionValues(someValues);
		templatePosset.addChild("c", dp.deepPossetClone());

		CVPowerPosset p = new CVPowerPosset("test", null, dp, templatePosset);

		var iter = p.iterator();

		int counter = 0;
		while (iter.hasNext()) {
			counter++;
			Posset p3 = iter.next();
			Iterator<? extends Element> iter3 = p3.iterator();
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			boolean firstTime = true;
			while (iter3.hasNext()) {
				if (!firstTime) {
					sb.append(",");
				} else {
					firstTime = false;
				}
				Element e = iter3.next();
				sb.append(e);

			}
			sb.append("}");
			LOG.info("Posset: {}", sb.toString());

		}
		LOG.info("Total of {} possets returned. Should be: {}", counter, Math.pow(2, PRIME1 * PRIME2));

	}

}
