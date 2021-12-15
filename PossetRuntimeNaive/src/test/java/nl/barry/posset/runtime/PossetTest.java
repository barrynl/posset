/**
 * 
 */
package nl.barry.posset.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Barry
 *
 */
public class PossetTest {

	public Posset getSimplePosset() {
		DerivedPosset dp = new DerivedPosset("main");
		dp.addPosset("third", new PrimePosset(7));
		dp.addPosset("first", new PrimePosset(2));
		dp.addPosset("second", new PrimePosset(3));

		return dp;
	}

	@Test
	public void testSimple() {

		Posset dp = getSimplePosset();
		Iterator<? extends Element> iter = dp.iterator();

		int cnt = 0;
		Element p = null;
		while ((p = iter.next()) != null) {
			System.out.println(p);
			cnt++;
		}

		assertEquals(7 * 2 * 3, cnt);
	}

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void testUnsupportedOperation() {
		expected.expect(UnsupportedOperationException.class);

		Posset dp = this.getSimplePosset();
		dp.iterator().hasNext();

	}
}
