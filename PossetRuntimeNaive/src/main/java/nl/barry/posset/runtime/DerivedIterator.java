/**
 * 
 */
package nl.barry.posset.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Barry
 * 
 */
public class DerivedIterator implements Iterator<Element> {

	private List<Iterator<? extends Element>> childIters;
	private Posset generatedByPosset;

	/**
	 * The current sub iterator where we're at.
	 */
	private int currentSubIterIdx = 0;

	public DerivedIterator(Posset aPosset, List<Iterator<? extends Element>> someChildIters) {
		generatedByPosset = aPosset;
		this.childIters = someChildIters;
	}

	/**
	 * @see nl.barry.posset.runtime.PossyIterator#reset()
	 */
	@Override
	public void reset() {
		this.currentSubIterIdx = 0;
		for (Iterator<? extends Element> iter : this.childIters) {
			iter.reset();
		}
	}

	public Possy next() {
		int lastIdx = this.childIters.size() - 1;
		Iterator<? extends Element> iter = this.childIters.get(this.currentSubIterIdx);
		Element next = iter.next();

		// only stop if the first iter has no next, or the last iter has
		// next.
		while (!(this.currentSubIterIdx == 0 && next == null) && !(this.currentSubIterIdx == lastIdx && next != null)) {

			if (this.currentSubIterIdx < lastIdx && next != null) {
				this.currentSubIterIdx++;
				this.childIters.get(this.currentSubIterIdx).reset();
			} else if (this.currentSubIterIdx > 0 && next == null) {
				this.currentSubIterIdx--;
			}

			iter = this.childIters.get(this.currentSubIterIdx);
			next = iter.next();
		}

		return this.current();
	}

	public Possy current() {
		DerivedPossy dp = null;
		Possy p;
		List<Possy> subs = new ArrayList<Possy>();
		for (Iterator<? extends Element> i : this.childIters) {
			p = (Possy) i.current();
			if (p == null) {
				return dp;
			}
			subs.add(p);
		}
		return new DerivedPossy(this.getPosset(), subs);
	}

	private Posset getPosset() {
		return this.generatedByPosset;
	}

	public boolean hasNext() {
		throw new UnsupportedOperationException(
				"This iterator only supports the next() method which will return NULL when no more elements are availabe.");
	}
}
