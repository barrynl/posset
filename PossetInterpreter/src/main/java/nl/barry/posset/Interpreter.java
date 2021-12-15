package nl.barry.posset;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.barry.posset.ast.PossetParser;
import nl.barry.posset.runtime.Element;
import nl.barry.posset.runtime.Iterator;
import nl.barry.posset.runtime.Posset;
import nl.barry.posset.runtime.PossetFactoryImpl;
import nl.barry.posset.runtime.Possy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import syntax.SyntaxException;

/**
 * Interprets the given source code. Typically, the process of interpreting is
 * repeating the loop:
 * <ul>
 * <li>processInput()</li>
 * <li>getPossies()</li>
 * <li>processOutput()</li>
 * </ul>
 * 
 * @author Barry
 * 
 */
public class Interpreter {

	private static final String MAIN = "main";

	private static Logger LOG = LoggerFactory.getLogger(Interpreter.class);

	private Map<String, Posset> runtimePossets;

	private Posset mainPosset;

	private PossetParser pp = null;

	private Map<String, nl.barry.posset.ast.Posset> nameMapping;

	public static void main(String[] args)
			throws FileNotFoundException, ParseException, nl.barry.posset.ast.ParseException {

		if (args.length != 1) {
			LOG.error("Provide one or more Posset source files.");
			System.exit(1);
		}

		System.out.println("Running: " + args[0]);

		try {
			Interpreter i = new Interpreter();
			i.loadSourceFile(new FileInputStream(args[0]));
			i.prepareExecution();

			List<Element> possies = i.getPossies();

			for (Element p : possies)
				System.out.println(p.toText());
			System.out.println("Size: " + possies.size());
		} catch (Exception e) {
			System.out.println("An error occurred while executing script: '" + args[0] + "'");
			e.printStackTrace();
		}

	}

	public Interpreter() {

		runtimePossets = new HashMap<String, Posset>();
		nameMapping = new HashMap<String, nl.barry.posset.ast.Posset>();
	}

	/**
	 * Lets you load one or more source files into memory.
	 * 
	 * @param aSource
	 * @throws nl.barry.posset.ast.ParseException
	 * @throws FileNotFoundException
	 * @throws ParseException
	 * @throws nl.barry.posset.ast.ParseException
	 */
	public void loadSourceFile(InputStream aSource) throws Exception {
		this.loadSourceFile(new InputStreamReader(aSource));
	}

	public void loadSourceFile(Reader aSource) throws Exception {
		if (pp == null) {
			pp = new PossetParser(aSource);
		} else {
			pp.ReInit(aSource);
		}

		List<nl.barry.posset.ast.Posset> possets = pp.Program();

		LOG.debug(possets.toString());

		for (nl.barry.posset.ast.Posset p : possets) {
			if (!nameMapping.containsKey(p.getName())) {
				nameMapping.put(p.getName(), p);
			} else {
				LOG.error("Posset with name '{}' is already defined.", p.getName());
			}
		}
	}

	public void prepareExecution() throws SyntaxException {
		// retrieve entrypoint (the posset named: main).
		if (nameMapping.containsKey(MAIN)) {
			mainPosset = new PossetFactoryImpl().getRuntimePosset(nameMapping, MAIN);
			LOG.info("Main Posset: {}", mainPosset);
		} else {
			LOG.error("Main Posset could not be found.");
		}
	}

	/**
	 * Make sure the input possets have their possies available.
	 */
	public void processInput() {
		// TODO not yet implemented
	}

	public List<Element> getPossies(int... max) {
		List<Element> possies = new ArrayList<Element>();

		int aMax = max.length > 0 ? max[0] : -1;

		if (mainPosset != null) {
			Iterator<? extends Element> i = mainPosset.iterator();
			int cnt = 0;
			long start = System.nanoTime();
			Element p;

			while (i.hasNext()) {
				cnt++;
				p = i.next();
				possies.add(p);
				if (cnt % 1 == 0)
					LOG.trace("{}\t:{}", cnt, p);

				if (aMax > 0 && cnt > aMax) {
					LOG.error("Stopping at {} possies to analyse infinte loop!", aMax);
					break;
				}
			}
			LOG.info("{} possets in {} ns", cnt, System.nanoTime() - start);
		}
		return possies;
	}

	/**
	 * @return The main (top-level) posset last loaded from source, or null if this
	 *         method is called before prepareExecution() method.
	 */
	public Posset getMainPosset() {
		return mainPosset;
	}

	/**
	 * Make sure the output possets process their possies into the output.
	 */
	public void processOutput() {
		// TODO not yet implemented
	}

}
