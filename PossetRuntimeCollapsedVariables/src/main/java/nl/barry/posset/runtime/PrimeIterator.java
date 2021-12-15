package nl.barry.posset.runtime;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimeIterator extends CollapsedVariablesIterator<Possy> {

	private static final Logger LOG = LoggerFactory.getLogger(PrimeIterator.class);

	private static final int PRE_START_ID = 0;

	private int currentId = PRE_START_ID;
	private int prime = -1;
	private String var = null;

	/**
	 * We keep track of which iterator gives us what restriction and if its
	 * different we actually check whether it results in valid value otherwise (if
	 * its equal) we return false. So, the second time we receive the same
	 * restriction, we return false.
	 */
	private Map<Parent, Map<String, Integer>> previousRestriction;

	@Override
	public String toString() {
		return "PrimeIterator [currentId=" + currentId + ", prime=" + prime + ", var=" + var + "]";
	}

	/**
	 * In case of collapsed variables, the primary parent iterator is the iterator
	 * that calls us the very first time and will actually dictate our progress. The
	 * other iterators (if any) will receive dummy results.
	 */
	private Parent primaryParentIterator = null;

	public PrimeIterator(CollapsedVariablesPosset aPosset, int aPrime, String aVar) {
		super(aPosset);
		this.prime = aPrime;
		this.var = aVar;

		this.previousRestriction = new HashMap<Parent, Map<String, Integer>>();

		assert (internalTests());
	}

	public boolean hasNext() {
		return this.hasNext(new HashMap<String, Integer>(), null);
	}

	private void storePrimaryParent(Parent parentIter) {

		// the first one to call us, is the our primary parent.
		if (this.primaryParentIterator == null) {
			this.primaryParentIterator = parentIter;
		}
	}

	private boolean isPrimaryParent(Parent parentIter) {
		if (parentIter == null || parentIter.equals(this.primaryParentIterator)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasNext(Map<String, Integer> restriction, Parent parentIter) {

		storePrimaryParent(parentIter);

		boolean hasNext = false;
		if (isRestricted(restriction, this.var)) {
			int value = this.getRestrictionValue(restriction, this.var);

			if (value >= 0) {
				// no conflicts
				assert (value > PRE_START_ID && value <= this.prime);
				hasNext = true;
			} else {
				// if value < 0, then there was a conflict during
				// getRestrictionValue().
				hasNext = false;
			}
		}

		if (isPrimaryParent(parentIter) && !isRestricted(restriction, this.var)) {
			hasNext = this.currentId < this.prime;
		} else if (isPrimaryParent(parentIter) && isRestricted(restriction, this.var)) {

			if (isFirstTime(parentIter, restriction)) {
				hasNext = getRestrictionValue(restriction, this.var) > PRE_START_ID;
			} else {
				return false;
			}

		} else if (!isPrimaryParent(parentIter) && !isRestricted(restriction, this.var)) {

			// alternate between true and false
			hasNext = isFirstTime(parentIter, restriction);
		} else if (!isPrimaryParent(parentIter) && isRestricted(restriction, this.var)) {
			if (isFirstTime(parentIter, restriction)) {
				return this.getRestrictionValue(restriction, this.var) == this.currentId;
			} else {
				return false;
			}
		}

		return hasNext;
	}

	/**
	 * check for repeating calls and return false if it is first time we receive a
	 * particular
	 */
	private boolean isFirstTime(Parent parentIter, Map<String, Integer> restriction) {

		boolean firstTime = false;

		Map<String, Integer> aPreviousRestriction = this.previousRestriction.get(parentIter);

		if (aPreviousRestriction != null && aPreviousRestriction.equals(restriction)) {
			// set it to null, to have it alternate
			this.previousRestriction.put(parentIter, null);
		} else {
			this.previousRestriction.put(parentIter, restriction);
			firstTime = true;
		}
		return firstTime;
	}

	/**
	 * Checks whether at least one variable of aVar is present in restriction.
	 * 
	 * @param incomingRestriction
	 * @param aVar
	 * @return
	 */
	private boolean isRestricted(Map<String, Integer> incomingRestriction, String aVar) {

		boolean contains = false;
		if (aVar != null) {
			String[] vars = aVar.split("&");

			for (String var : vars) {
				contains |= incomingRestriction.containsKey(var);
			}
		}
		return contains;
	}

	public Possy next() {
		return this.next(new HashMap<String, Integer>(), null);
	}

	@Override
	public Possy next(Map<String, Integer> restriction, Parent parentIterator) {
		Possy p = null;

		if (isPrimaryParent(parentIterator)) {
			if (!this.isRestricted(restriction, this.var)) {
				this.currentId++;
			} else {
				this.currentId = this.getRestrictionValue(restriction, this.var);
			}
		}

		p = this.current(restriction);
		// assert p != null :
		// "If the restriction is invalid, the hasNext should have failed.";
		return p;
	}

	@Override
	public void reset() {
		this.reset(null);
	}

	@Override
	public void reset(Parent parentIter) {
		if (isPrimaryParent(parentIter)) {
			this.currentId = PRE_START_ID;
			this.primaryParentIterator = null;
		}
		this.previousRestriction.remove(parentIter);
	}

	public Possy current(Map<String, Integer> restriction) {
		Possy p = null;

		if (!this.isRestricted(restriction, this.var)) {
			p = new PrimePossy(this.getPosset(), this.currentId);
		} else {
			int value = this.getRestrictionValue(restriction, this.var);
			if ((value >= PRE_START_ID && value == this.currentId)) {
				p = new PrimePossy(this.getPosset(), this.currentId);
			}
		}

		return p;
	}

	/**
	 * Returns the value if all variables in someVar are contained in restriction
	 * and agree on the value to give, returns -1 otherwise.
	 * 
	 * @param restriction
	 * @param someVar
	 * @return
	 */
	public int getRestrictionValue(Map<String, Integer> restriction, String someVar) {
		int value = 0, otherValue;

		String[] splitVar = someVar.split("&");

		for (String aVar : splitVar) {
			if (restriction.containsKey(aVar)) {
				otherValue = restriction.get(aVar);
				if (value > 0) {
					if (otherValue != value) {
						return -1;
					}
				} else {
					value = otherValue;
				}
			}
		}
		return value;
	}

	@Override
	public Possy current() {
		return this.current(new HashMap<String, Integer>());
	}

	/**
	 * Is this a good way to test internal methods? Instead of wanting to test them
	 * in a unit test?
	 * 
	 * I don't think so, because officially you should test your private methods
	 * through your public methods. The time you put into creating these internal
	 * tests, should be put into your public tests and the public tests are a very
	 * valuable asset.
	 * 
	 * @return
	 */
	private boolean internalTests() {

		Map<String, Integer> restriction = new HashMap<String, Integer>();
		restriction.put("2A", 1);
		restriction.put("2B", 1);
		assert (isRestricted(restriction, "2A&2B"));
		assert (getRestrictionValue(restriction, "2A&2B") == 1);

		restriction.put("2B", 2);
		assert (this.isRestricted(restriction, "2A&2B"));
		assert (this.getRestrictionValue(restriction, "2A&2B") == -1);

		return true;
	}
}
