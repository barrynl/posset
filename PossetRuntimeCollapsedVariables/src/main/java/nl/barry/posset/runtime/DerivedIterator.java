package nl.barry.posset.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DerivedIterator extends CollapsedVariablesIterator<Element> {

	private List<CollapsedVariablesIterator<? extends Element>> childIters;
	/**
	 * The current sub iterator where we're at.
	 */
	private int currentSubIterIdx = -1;

	private int firstSubIterIdx = -1;

	private int lastSubIterIdx = -1;

	private Parent primaryParentIterator;

	/**
	 * A list of restriction which this iterator should dictate on its sub iters.
	 */
	private Map<String, List<Integer>> restriction = null;
	private int restrictionIndex = -1;

	/**
	 * We keep track of which iterator gives us what restriction and if its
	 * different we actually check whether it results in valid value otherwise (if
	 * its equal) we return false. So, the second time we receive the same
	 * restriction, we return false.
	 */
	private Map<Parent, Map<String, Integer>> previousRestriction;

	/**
	 * Initialize the iterator with the posset to which it belongs, the iterators
	 * from which it is derived and an optional restriction (null means no
	 * restriction).
	 * 
	 * @param aPosset
	 * @param someNames    The names of the possets of the sub iterators of this
	 *                     iterator. The size should be equal to the size of
	 *                     someIters.
	 * @param someIters    The sub iterators of this iterator. The size should be
	 *                     equal to the size of someNames.
	 * @param aRestriction
	 */
	public DerivedIterator(CollapsedVariablesPosset aPosset, List<String> someNames,
			List<CollapsedVariablesIterator<? extends Element>> someIters, Map<String, List<Integer>> aRestriction) {
		super(aPosset);

		assert (someNames.size() == someIters.size());
		this.childIters = someIters;

		this.restriction = aRestriction;
		if (this.restriction == null) {
			this.firstSubIterIdx = 0;
		} else {
			this.firstSubIterIdx = -1;
		}

		this.lastSubIterIdx = childIters.size() - 1;

		this.previousRestriction = new HashMap<Parent, Map<String, Integer>>();
	}

	public DerivedIterator(CollapsedVariablesPosset aPosset, List<String> someNames,
			List<CollapsedVariablesIterator<? extends Element>> someIters) {
		this(aPosset, someNames, someIters, null);
	}

	/**
	 * We try to see the problem as a tree. Where each depth level corresponds to an
	 * subIterator. We then traverse the tree through a depth-first search with
	 * backtracking until we reach a leaf. Then we have a next() item.
	 * 
	 * Check if the posset iterator has a next item while taking restrictions into
	 * consideration.
	 */
	public boolean hasNext(Map<String, Integer> incomingRestriction, Parent parentIterator) {

		// the first one to call this method is our primary parent iterator, the
		// rest are secondary.
		if (parentIterator != null && this.primaryParentIterator == null) {
			this.setPrimaryParentIterator(parentIterator);
		}

		Map<String, Integer> mergedRestriction = this.currentRestriction(this.restrictionIndex, incomingRestriction);

		// check for repeating calls and return false if it is not the first
		// time we receive a particular restriction.
		if (parentIterator != null && !parentIterator.equals(this.primaryParentIterator)) {
			Map<String, Integer> aPreviousRestriction = this.previousRestriction.get(parentIterator);
			if (aPreviousRestriction != null && aPreviousRestriction.equals(incomingRestriction)) {
				this.previousRestriction.put(parentIterator, null);
				return false;
			} else {
				this.previousRestriction.put(parentIterator, incomingRestriction);
			}

			// dummy mode active
			return this.current(mergedRestriction) != null;
		}

		boolean hasNext = true;

		CollapsedVariablesIterator<? extends Element> iter = null;
		if (this.currentSubIterIdx >= 0) {
			// normal execution
			iter = this.childIters.get(this.currentSubIterIdx);
			hasNext = iter.hasNext(mergedRestriction, new Parent(this, this.currentSubIterIdx));
		} else {
			// < 0: execution with restrictions
			hasNext = this.hasNextRestriction(incomingRestriction);
			iter = null;
		}

		// only stop if the first iter has no next, or the last iter has
		// next.
		while (!(this.currentSubIterIdx == this.firstSubIterIdx && !hasNext)
				&& !(this.currentSubIterIdx == this.lastSubIterIdx && hasNext)) {

			if (this.currentSubIterIdx < this.lastSubIterIdx && hasNext) {

				if (this.currentSubIterIdx >= 0) {
					// normal execution
					iter.next(mergedRestriction, new Parent(this, this.currentSubIterIdx));
				} else {
					// restriction execution
					mergedRestriction = this.nextRestriction(incomingRestriction);
				}
				this.childIters.get(this.currentSubIterIdx + 1).reset(new Parent(this, this.currentSubIterIdx + 1));
				this.currentSubIterIdx++;
			} else if (this.currentSubIterIdx > this.firstSubIterIdx && !hasNext) {
				this.currentSubIterIdx--;
			}

			if (this.currentSubIterIdx >= 0) {
				// normal execution
				iter = this.childIters.get(this.currentSubIterIdx);
				hasNext = iter.hasNext(mergedRestriction, new Parent(this, this.currentSubIterIdx));
			} else {
				// < 0: execution with restrictions
				hasNext = this.hasNextRestriction(incomingRestriction);
				iter = null;
			}
		}

		return hasNext;
	}

	@Override
	public String toString() {
		return "DerivedIterator [currentSubIterIdx=" + currentSubIterIdx + ", restriction=" + restriction
				+ ", getPosset()=" + getPosset() + "]";
	}

	/**
	 * Retrieve the current restriction if restrictions apply to this iterator,
	 * otherwise just return a copy of the incoming restriction. If a conflict
	 * occurs, this method returns null.
	 * 
	 * @param aRestriction
	 * @return
	 */
	private Map<String, Integer> currentRestriction(int aRestrictionIndex, Map<String, Integer> aRestriction) {

		Map<String, Integer> newRestriction = new HashMap<String, Integer>(aRestriction);

		if (restriction != null && aRestrictionIndex >= 0) {
			for (String var : this.restriction.keySet()) {
				List<Integer> values = this.restriction.get(var);

				if (newRestriction.containsKey(var)) {
					// already there, is this a conflict?
					Integer existingOne = newRestriction.get(var);
					Integer currentOne = values.get(aRestrictionIndex);

					if (!existingOne.equals(currentOne)) {
						// conflict!
						return null;
					}
					// do nothing otherwise.
				} else {
					newRestriction.put(var, values.get(aRestrictionIndex));
				}
			}
		}

		return newRestriction;
	}

	/**
	 * Returns the next restriction. Always make sure hasNextRestriction returns
	 * true before calling this method.
	 * 
	 * @param aRestriction
	 * @return the next restriction if there is any or null otherwise.
	 */
	private Map<String, Integer> nextRestriction(Map<String, Integer> aRestriction) {

		assert this.hasNextRestriction(
				aRestriction) : "Check whether hasNextRestriction returns true before calling nextRestriction().";

		Map<String, Integer> nextOne = null;
		do {
			this.restrictionIndex++;
			nextOne = this.currentRestriction(this.restrictionIndex, aRestriction);
		} while (nextOne == null);
		return nextOne;
	}

	/**
	 * Checks whether the restriction index is already at the end of the list of
	 * values.
	 * 
	 * @param aRestriction
	 * @return false if there are no more restrictions available for this iterator.
	 */
	private boolean hasNextRestriction(Map<String, Integer> aRestriction) {

		boolean hasNext = false;
		List<Integer> values;
		java.util.Iterator<String> iter = this.restriction.keySet().iterator();

		// how many restrictions are there for this posset is there still a
		// valid one.
		if (iter.hasNext()) {
			String var = iter.next();
			values = this.restriction.get(var);
			// not only check the index, also whether there is still at least
			// one non conflicting value left.
			if ((values.size() - 1) > this.restrictionIndex) {

				for (int i = this.restrictionIndex + 1; i < values.size() && !hasNext; i++) {
					if (this.currentRestriction(i, aRestriction) != null) {
						hasNext = true;
					}
				}
			}
		}
		return hasNext;
	}

	public Element next(Map<String, Integer> restriction, Parent parentIterator) {

		Map<String, Integer> newRestriction = this.currentRestriction(this.restrictionIndex, restriction);

		if (parentIterator != null && !parentIterator.equals(this.primaryParentIterator)) {
			// dummy mode, we've already checked whether that we do not get NULL
			// possy
			// (see hasNext())

			Element p = this.current(newRestriction);
			assert (p != null);
			return p;
		}

		// only iterator further if maxcount is reached.
		int lastIdx = this.childIters.size() - 1;
		this.childIters.get(lastIdx).next(newRestriction, new Parent(this, lastIdx));
		Element p = this.current(newRestriction);
		return p;
	}

	@Override
	public void reset(Parent parentIterator) {

		if (parentIterator != null && !parentIterator.equals(this.primaryParentIterator)) {
			// dummy mode, we only reset when the calling iterator is the
			// primary one.

			this.previousRestriction.remove(parentIterator);

			return; // we do nothing
		}

		if (restriction != null) {
			this.currentSubIterIdx = -1;
		} else {
			this.currentSubIterIdx = 0;
		}

		primaryParentIterator = null;
		previousRestriction.remove(parentIterator);

		this.restrictionIndex = -1;
		for (int i = 0; i < this.childIters.size(); i++) {
			CollapsedVariablesIterator<? extends Element> iter = this.childIters.get(i);
			iter.reset(new Parent(this, i));
		}
	}

	@Override
	public Element current(Map<String, Integer> restriction) {

		Map<String, Integer> currentRestriction = this.currentRestriction(restrictionIndex, restriction);

		List<Element> subs = new ArrayList<Element>();

		Element p;
		for (CollapsedVariablesIterator<? extends Element> i : this.childIters) {
			p = i.current(currentRestriction);
			if (p != null) {
				subs.add(p);
			} else {
				return null;
			}
		}
		return new DerivedPossy(this.getPosset(), subs);
	}

	/**
	 * Keeps track of the parent iterator that first created this iterator and thus
	 * will be the iterator that actually changes this iterator if multiple parent
	 * iterators are available (i.e. if variables are collapsed multiple parents are
	 * allocated). The oter iterator that are not the primary, will only receive
	 * dummy values from the this iterator. hasNext will iterate between true and
	 * false when in dummy mode and next() will return the current possy instead of
	 * the next when in dummy mode. Note that it will (!) check if the incoming
	 * restriction conflicts and this might possibly return a NULL value.
	 * 
	 * @param iter
	 */
	public void setPrimaryParentIterator(Parent iter) {
		this.primaryParentIterator = iter;
	}
}
