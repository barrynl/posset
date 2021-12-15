/**
 * 
 */
package nl.posset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * Our Posset language, consists of a combinatin of three features: - the first
 * is derived sets. So, sets are build from from other sets. - the second is
 * meta sets. So, sets of sets are build via the Power Set operator. - the third
 * is variables. Variables allow parts of different derived sets to stay and
 * remain connected.
 * 
 * @author nouwtb
 *
 */
public class MetaPosset {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Set<String> fruits = new HashSet<>();
		fruits.add("Apple");
		fruits.add("Orange");
		fruits.add("Banana");
		fruits.add("Kiwi");
		fruits.add("Mango");
		fruits.add("Peach");

		Set<List<Boolean>> binarySubsets = createBinarySubsets(fruits.size());
		
		System.out.println(binarySubsets);
		
		List<String> fruitList = new ArrayList<String>(fruits);

		Set<Set<String>> fruitsPS = createPowerSet(fruitList, binarySubsets);

		System.out.println(fruitsPS);
		System.out.println(fruitsPS.size());

	}

	private static Set<Set<String>> createPowerSet(List<String> fruits, Set<List<Boolean>> binarySubsets) {

		Set<Set<String>> powerSet = new HashSet<>();
		Set<String> subSet;
		for (List<Boolean> binaryList : binarySubsets) {
			subSet = new HashSet<>();
			for (int i = 0; i < binaryList.size(); i++) {
				Boolean b = binaryList.get(i);
				if (b) {
					subSet.add(fruits.get(i));
				}
			}
			powerSet.add(subSet);
		}
		return powerSet;
	}

	private static Set<List<Boolean>> createBinarySubsets(int setSize) {

		// what is minimum int that you can represent {@code setSize} bits? 0
		// what is maximum int that you can represent {@code setSize} bits? 2^setSize

		int start = 0;
		int end = 1 << setSize;
		Set<List<Boolean>> set = new HashSet<>(end);
		String endBin = Integer.toBinaryString(end);

		for (int i = start; i < end; i++) {
			String bin = toBinary(i, endBin.length() - 1);
			List<Boolean> bools = fromCharToBoolean(bin);
			set.add(bools);
		}

		return set;
	}

	private static String toBinary(int val, int len) {
		return Integer.toBinaryString((1 << len) | val).substring(1);

	}

	private static List<Boolean> fromCharToBoolean(String in) {
		List<Boolean> list = new ArrayList<>();

		for (int i = 0; i < in.length(); i++) {
			char charAt = in.charAt(i);
			if (charAt == '0') {
				list.add(Boolean.valueOf(false));
			} else if (charAt == '1') {
				list.add(Boolean.valueOf(true));
			} else {
				throw new IllegalArgumentException("The input string should only contains 0s or 1s and not: " + charAt);
			}
		}

		return list;
	}

}
