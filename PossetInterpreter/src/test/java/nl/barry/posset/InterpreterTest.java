package nl.barry.posset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import nl.barry.posset.runtime.CollapsedVariablesPosset;
import nl.barry.posset.runtime.DerivedPosset;
import nl.barry.posset.runtime.DerivedPossy;
import nl.barry.posset.runtime.Element;
import nl.barry.posset.runtime.Posset;
import nl.barry.posset.runtime.PrimePosset;
import syntax.SyntaxException;

public class InterpreterTest {

	private static final Logger LOG = LoggerFactory.getLogger(InterpreterTest.class);

	@Test
	public void testAggregation()
			throws Exception {
		Interpreter i = new Interpreter();
		Class<InterpreterTest> claz = InterpreterTest.class;
		i.loadSourceFile(claz.getResourceAsStream("/Aggregation.pos"));

		i.prepareExecution();
		List<Element> possies = i.getPossies();

		for (Element p : possies) {
			LOG.info("{}", ((DerivedPossy) p).getSubPossy("d"));
		}
		LOG.info("Nr of possies: {}", possies.size());
	}

	@Test
	public void testJavaGreaterThan() {

		long start = System.currentTimeMillis();

		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++) {
			if (!(i > 5)) {
				list.add(i);
			}
		}
		long end = System.currentTimeMillis();

		LOG.info("Answers: {}", list);

		LOG.info("Duration: {}ms", end - start);
	}

	@Test
	public void testMultiDigitPlus()
			throws Exception {

		long start = System.currentTimeMillis();

		Interpreter i = new Interpreter();

		Class<InterpreterTest> claz = InterpreterTest.class;

		i.loadSourceFile(claz.getResourceAsStream("/boolean.pos"));
		i.loadSourceFile(claz.getResourceAsStream("/singleDigit.pos"));

		StringReader sr = new StringReader("main ( GreaterThanOrEqual g (p2[A];p3[B];) Five f[A]; False t[B]; )");
		i.loadSourceFile(sr);

		i.prepareExecution();
		List<Element> possies = i.getPossies();

		long end = System.currentTimeMillis();

		for (Element p : possies) {
			if (p instanceof DerivedPossy) {
				DerivedPossy dp = (DerivedPossy) p;
				LOG.info("g.p1: {}", dp.getSubPossy("g.p1"));
			}
		}

		LOG.info("Duration: {}ms", end - start);
	}

	@Test
	public void testBrainfuckInterpreter()
			throws Exception {
		Interpreter i;
		int nrOfPossies = 10;

//		while (nrOfPossies == 10) {
		i = new Interpreter();
		LOG.info("(re)starting...");

		// TODO maybe the fluctuating possets are caused by variabels that should be
		// combined after replacement.
		// I thought this would not be necessary, but it seems like it is. See info on
		// desktop

		Class<InterpreterTest> claz = InterpreterTest.class;

		i.loadSourceFile(claz.getResourceAsStream("/singleDigit.pos"));
		i.loadSourceFile(claz.getResourceAsStream("/boolean.pos"));
		i.loadSourceFile(claz.getResourceAsStream("/BrainfuckInterpreter.pos"));

		i.prepareExecution();
		List<Element> possies = i.getPossies(/* 999 */);

		nrOfPossies = possies.size();

		for (Element p : possies) {
			LOG.info("{}", ((DerivedPossy) p).getSubPossy("b.tape"));
		}
		LOG.info("Nr of possies: {}", possies.size());
//		}
	}

	@Test
	public void testDigitUnion()
			throws Exception {
		Interpreter i = new Interpreter();
		Class<InterpreterTest> claz = InterpreterTest.class;
		i.loadSourceFile(claz.getResourceAsStream("/boolean.pos"));
		i.loadSourceFile(claz.getResourceAsStream("/singleDigit.pos"));
		i.loadSourceFile(claz.getResourceAsStream("/DigitUnion.pos"));

		i.prepareExecution();
		List<Element> possies = i.getPossies();

		boolean firstTime = true;
		for (Element p : possies) {
			if (firstTime)
				firstTime = false;
			LOG.info("{}", ((DerivedPossy) p).getSubPossy("d"));
		}
		LOG.info("Nr of possies: {}", possies.size());
	}

	/*
	 */
	public boolean possetEquals(Posset o1, Posset o2) {

		boolean isEqual = false;
		if (o2 instanceof PrimePosset && o1 instanceof PrimePosset) {
			PrimePosset pp2 = (PrimePosset) o2;
			PrimePosset pp1 = (PrimePosset) o1;

			if (pp2.getPrime() == pp1.getPrime()) {

				String[] pp2Vars = pp2.getVar().split("&");
				String[] pp1Vars = pp1.getVar().split("&");

				Arrays.sort(pp2Vars);
				Arrays.sort(pp1Vars);
				if (Arrays.equals(pp2Vars, pp1Vars)) {
					isEqual = true;
				} else {
					StringBuilder sb = new StringBuilder();
					CollapsedVariablesPosset parent = pp1;
					do {
						sb.insert(0, parent.getName());
						sb.insert(0, " / ");

						Iterator<CollapsedVariablesPosset> iter = parent.getParents().iterator();

						if (iter.hasNext()) {
							parent = iter.next();
						} else {
							parent = null;
						}
					} while (parent != null);

					LOG.error("The PrimePosset {} variables are not equal {} versus {}.",
							new Object[] { sb.toString(), pp1Vars, pp2Vars });
				}
			} else {
				LOG.error("Primes are not equal");
			}
		} else if (o2 instanceof DerivedPosset && o1 instanceof DerivedPosset) {
			DerivedPosset dp2 = (DerivedPosset) o2;
			DerivedPosset dp1 = (DerivedPosset) o1;
			if (o1.getName().equals(dp2.getName())) {
				if (dp1.getChildNames().equals(dp2.getChildNames())) {
					boolean allEqual = true;
					for (String name : dp1.getChildNames()) {
						if (!possetEquals(dp1.getChildPosset(name), dp2.getChildPosset(name))) {
							allEqual = false;
							// LOG.error("ChildPossets are not equal");
						}
					}

					// TODO also check the restriction

					isEqual = allEqual;
				} else {
					LOG.error("Child names are not equal.");
				}
			} else {
				LOG.error("Should either be a PrimePosset or a DerivedPosset.");
			}
		}

		return isEqual;
	}

	/**
	 * Temporary method to analyse the result of
	 * FluctuatingNumberOfPossiesReturned.pos better.
	 */
	private void analyseTable(Table<Integer, Integer, String> t) {
		Table<Integer, Integer, String> newTable = HashBasedTable.create();

		// merge columns
		replaceByDigit(t, 1, 2, 1, newTable, 1);
		replaceByDigit(t, 3, 4, 1, newTable, 2);
		replaceByDigit(t, 5, 6, 1, newTable, 3);
		replaceByBoolean(t, 7, 2, newTable, 4);
		replaceByDigit(t, 8, 9, 2, newTable, 5);
		replaceByDigit(t, 10, 11, 2, newTable, 6);
		replaceByBoolean(t, 12, 3, newTable, 7);
		replaceByDigit(t, 13, 14, 3, newTable, 8);
		replaceByDigit(t, 15, 16, 3, newTable, 9);
		replaceByDigit(t, 17, 18, 3, newTable, 10);
		replaceByDigit(t, 19, 20, 3, newTable, 11);
		replaceByBoolean(t, 21, 2, newTable, 12);
		replaceByBoolean(t, 22, 2, newTable, 13);
		replaceByBoolean(t, 23, 2, newTable, 14);
		replaceByBoolean(t, 24, 2, newTable, 15);
		replaceByBoolean(t, 25, 2, newTable, 16);
		replaceByDigit(t, 26, 27, 2, newTable, 17);
		replaceByDigit(t, 28, 29, 2, newTable, 18);
		replaceByBoolean(t, 30, 3, newTable, 19);
		replaceByDigit(t, 31, 32, 3, newTable, 20);
		replaceByDigit(t, 33, 34, 3, newTable, 21);
		replaceByDigit(t, 35, 36, 3, newTable, 22);
		replaceByDigit(t, 37, 38, 3, newTable, 23);

		printTable(newTable);
	}

	private void printTable(Table<Integer, Integer, String> aTable) {
		int row = 1;
		int col = 1;
		Map<Integer, String> rowMap = null;
		while (!(rowMap = aTable.row(row)).isEmpty()) {
			String val = null;
			while ((val = rowMap.get(col)) != null) {
				System.out.print(val);
				System.out.print(";");
				col++;
			}
			System.out.print("\n");
			row++;
			col = 1;
		}
	}

	/**
	 * Convert the given column of table t into the boolean value true/false in
	 * table newTable.
	 * 
	 * @param t
	 * @param newTable
	 * @param sourceFirstCol
	 * @param second
	 */
	private void replaceByBoolean(Table<Integer, Integer, String> t, int sourceFirstCol, int sourceHeaderRow,
			Table<Integer, Integer, String> newTable, int target) {
		Map<Integer, String> column1 = t.column(sourceFirstCol);
		newTable.put(1, target, column1.get(sourceHeaderRow));

		List<Integer> col1rows = new ArrayList<Integer>(column1.keySet());
		Collections.sort(col1rows);

		for (Integer col1row : col1rows) {
			if (col1row > 6) {
				String col1val = column1.get(col1row);
				newTable.put(col1row - 5, target, "" + possyToBoolean(col1val));
			}
		}
	}

	/**
	 * Convert the given two columns of table t to the actual digit it represents
	 * into table newTable.
	 * 
	 * @param t
	 * @param newTable
	 * @param sourceFirstCol
	 * @param sourceSecondCol
	 */
	private void replaceByDigit(Table<Integer, Integer, String> t, int sourceFirstCol, int sourceSecondCol,
			int sourceHeaderRow, Table<Integer, Integer, String> newTable, int target) {
		Map<Integer, String> column1 = t.column(sourceFirstCol);
		newTable.put(1, target, column1.get(sourceHeaderRow));
		Map<Integer, String> column2 = t.column(sourceSecondCol);

		List<Integer> col1rows = new ArrayList<Integer>(column1.keySet());
		Collections.sort(col1rows);

		for (Integer col1row : col1rows) {
			if (col1row > 6) {
				String col1val = column1.get(col1row);
				String col2val = column2.get(col1row);
				newTable.put(col1row - 5, target, "" + possyToDigit(col1val + col2val));
			}
		}
	}

	private Boolean possyToBoolean(String aPossy) {
		switch (aPossy) {
		case "1":
			return true;
		case "2":
			return false;
		}
		return false;
	}

	private Integer possyToDigit(String aPossy) {
		switch (aPossy) {
		case "11":
			return 1;
		case "12":
			return 2;
		case "13":
			return 3;
		case "14":
			return 4;
		case "15":
			return 5;
		case "21":
			return 6;
		case "22":
			return 7;
		case "23":
			return 8;
		case "24":
			return 9;
		case "25":
			return 0;
		}
		return -1;
	}

}
