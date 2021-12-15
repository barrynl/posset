/**
 * 
 */
package nl.barry.posset.debug;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.barry.posset.runtime.CVPowerPosset;
import nl.barry.posset.runtime.CollapsedVariablesPosset;
import nl.barry.posset.runtime.DerivedPosset;
import nl.barry.posset.runtime.DerivedPossy;
import nl.barry.posset.runtime.Element;
import nl.barry.posset.runtime.Posset;
import nl.barry.posset.runtime.Possy;
import nl.barry.posset.runtime.PrimePossy;

/**
 * This class provides printing methods for the interpreter to help debugging
 * applications.
 * 
 * @author Barry
 *
 */
public class Printer {

	private static final String SEPARATOR = ";";

	public static void possyPrinter(Element p, boolean printHeader, PrintStream os) {

		if (printHeader)
			printHeader(getPossyHeader(p), os);

		printPossy(p, os);
		os.println();

	}

	private static void printPossy(Element p, PrintStream os) {
		if (p instanceof DerivedPossy) {

			DerivedPossy dp = (DerivedPossy) p;
			for (Element child : dp.getSubPossies()) {

				printPossy(child, os);
			}

		} else if (p instanceof DerivedPosset) {
			DerivedPosset dp = (DerivedPosset) p;
			os.print(dp.toText());
		} else {
			PrimePossy pp = (PrimePossy) p;

			os.print(pp.getId());
			os.print(SEPARATOR);
		}
	}

	private static void printHeader(Map<String, Object> possyMap, PrintStream os) {

		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> otherMapList = new ArrayList<Map<String, Object>>();
		mapList.add(possyMap);

		List<String> headerNames = new ArrayList<String>();

		while (mapList.size() > 0) {

			for (Map<String, Object> map : mapList) {
				if (map != null) {
					for (Map.Entry<String, Object> entry : map.entrySet()) {
						String name = entry.getKey();
						Object mapObject = entry.getValue();

						if (mapObject != null) {
							Map<String, Object> childMap = (Map<String, Object>) mapObject;

							int count = getPossyWidth(childMap);

							os.print(name);
							for (int i = 0; i < count; i++) {
								os.print(SEPARATOR);
							}

							otherMapList.add(childMap);

						} else {
							// child is null, so we just print the object.
							os.print(name);
							os.print(SEPARATOR);
							otherMapList.add(null);
							headerNames.add(name);
						}
					}
				} else {
					os.print(SEPARATOR);
				}
			}
			os.println();

			mapList.clear();
			mapList.addAll(otherMapList);
			otherMapList.clear();
		}
	}

	private static int getPossyWidth(Map<String, Object> possyMap) {
		int width = 0;
		for (Map.Entry<String, Object> entry : possyMap.entrySet()) {
			Object mapObject = entry.getValue();

			if (mapObject != null) {
				Map<String, Object> map = (Map<String, Object>) mapObject;

				width += getPossyWidth(map);
			} else {
				width++;
			}
		}
		return width;
	}

	// recursive
	private static Map<String, Object> getPossyHeader(Element p) {
		Map<String, Object> header = new LinkedHashMap<String, Object>();

		if (p.isPosset()) {
			CVPowerPosset posset = (CVPowerPosset) p.getGeneratedByPosset();
			header.put("powerOf" + (posset.getVar() != null ? "[" + posset.getVar() + "]" : ""), null);
		} else if (p instanceof DerivedPossy) {
			DerivedPosset posset = (DerivedPosset) p.getGeneratedByPosset();
			for (Element possy : ((DerivedPossy) p).getSubPossies()) {
				for (String name : posset.getChildNames()) {
					Posset child = posset.getChildPosset(name);
					if (possy.getGeneratedByPosset().equals(child)) {
						CollapsedVariablesPosset cvPosset = (CollapsedVariablesPosset) possy.getGeneratedByPosset();

						header.put(name + (cvPosset.getVar() != null ? "[" + cvPosset.getVar() + "]" : ""),
								getPossyHeader(possy));
					}
				}
			}
		} else {
			assert (p instanceof PrimePossy);

			CollapsedVariablesPosset cvPosset = (CollapsedVariablesPosset) p.getGeneratedByPosset();

			header.put(cvPosset.getName() /*
											 * + (cvPosset.getVar() != null ? "(" + cvPosset.getVar() + ")" : "")
											 */, null);
		}
		return header;
	}

}
