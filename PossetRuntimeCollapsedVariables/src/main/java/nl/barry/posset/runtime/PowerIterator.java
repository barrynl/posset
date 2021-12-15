package nl.barry.posset.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import syntax.ChildAlreadyExistsException;

/**
 * The iterator of the power set works like this. We start with the empty set
 * (which we keep in a previously returned sets set) and then we follow the
 * following procedure:
 * <ul>
 * <li>take an element from the powerOfPosset (if any)</li>
 * <li>add it to every set in our previously returned sets set</li>
 * <li>return each of these newly created sets</li>
 * <li>if there are no more sets left to combine with, repeat from the top.</li>
 * <ul>
 * 
 * @author nouwtb
 *
 */
public class PowerIterator extends CollapsedVariablesIterator<CollapsedVariablesPosset> {

	private static final Logger LOG = LoggerFactory.getLogger(PowerIterator.class);

	private CollapsedVariablesPosset powerOfPosset;
	private CollapsedVariablesIterator<? extends Element> powerOfPossetIterator;
	private int powerOfPossetIteratorIndex = -1;

	/**
	 * A (growing) set of possets that were previously produced by this iterator.
	 * Every time we get a new possy from our powerOfPosset, we copy all the
	 * existing sets and add the new posset (representing this possy) to the set.
	 */
	private Set<DerivedPosset> previousPossets;

	/**
	 * The posset that is returned by the last next() call and will be returned by a
	 * current() call. It will be moved to the previousPossets, once a new
	 * currentPreviousPosset is created.
	 */
	private DerivedPosset currentPosset;

	/**
	 * A set which is a copy of the previousPossets set and starts when a new next()
	 * Possy has been retrieved from the powerOfPosset that needs to be added to all
	 * previously returned possets. Every next() call of this iterator we remove one
	 * of the elements from this set and add it (concatenated with the new possy) to
	 * the previousPossets set.
	 */
	private Set<DerivedPosset> previousPossetsCopy;

	/**
	 * The template posset is used as a template to create the posset copies. They
	 * have the powerOfPosset as a child and have restriction headers all setup
	 * correctly. In this iterator we give them the correct restriction values to
	 * behave the way we want (and be the subset of the powerset we want it to be).
	 */
	private DerivedPosset templatePosset;

	public PowerIterator(CollapsedVariablesPosset aGeneratedByPosset, CollapsedVariablesPosset aPowerOfPosset,
			DerivedPosset aTemplatePosset) {
		super(aGeneratedByPosset);

		this.templatePosset = aTemplatePosset;
		this.previousPossets = new HashSet<DerivedPosset>();
		this.powerOfPosset = aPowerOfPosset;
		this.powerOfPossetIterator = powerOfPosset.iterator();
		this.previousPossetsCopy = new HashSet<>();
	}

	@Override
	public boolean hasNext(Map<String, Integer> restriction, Parent parentIterator) {

		if (currentPosset == null) {
			// we always have at least the empty set.
			// every hasNext should 'do' something with the powerOfPosset, because otherwise
			// it interferes with the normal process of iteration.
			// we will just add all of them to the previousPossets at once.

			// we call the powerPosset iterator, to let it know *we* are its primary parent
			if (powerOfPossetIterator.hasNext(new HashMap<String, Integer>(), new Parent(this, 0))) {
				powerOfPossetIterator.next(new HashMap<String, Integer>(), new Parent(this, 0));
			}
			return true;
		} else if (isEmptySet(this.currentPosset) && powerOfPossetIterator.current(restriction) != null) {
			// we return the current powerOfPosset possibility.
			return true;
		} else if (!previousPossetsCopy.isEmpty()) {
			// we already started, but are there any possets lefts.
			return true;
		} else if (powerOfPossetIterator.hasNext(new HashMap<String, Integer>(), new Parent(this, 0))) {
			return true;
		} else {
			// nothing
		}
		return false;
	}

	/**
	 * Check if the restrictions of the DerivedPosset are empty.
	 * 
	 * @param posset
	 * @return
	 */
	private boolean isEmptySet(DerivedPosset posset) {
		boolean isEmpty = true;

		List<List<Integer>> values = posset.getRestrictionValues();

		for (List<Integer> col : values) {
			isEmpty &= col.isEmpty();
		}

		return isEmpty;
	}

	@Override
	public CollapsedVariablesPosset current(Map<String, Integer> restriction) {
		return this.currentPosset;
	}

	@Override
	public DerivedPosset next(Map<String, Integer> restriction, Parent parentIterator) {
		DerivedPosset nextOne = null;
		if (currentPosset == null) {
			// the template posset is a posset with restriction headers, but 0 restriction
			// values. I.e. it is the empty set.
			nextOne = this.templatePosset;
		} else if (isEmptySet(this.currentPosset) && powerOfPossetIterator.current(restriction) != null) {
			Element e = this.powerOfPossetIterator.current(restriction);
			if (e.isPosset()) {
				throw new UnsupportedOperationException("Power possets of power possets not yet supported!");
			} else {
				Possy p = (Possy) e;

				List<List<Integer>> newValues = findCorrespondingRestrictionValues(this.currentPosset, p);

				try {
					nextOne = templatePosset.deepPossetClone();
					nextOne.setRestrictionValues(newValues);
				} catch (ChildAlreadyExistsException e1) {
					LOG.error("An error occurred while cloning posset {}", templatePosset, e1);
				}
			}
			this.previousPossets.add(this.currentPosset);

		} else if (!this.previousPossetsCopy.isEmpty()) {
			// take (and remove) the next posset from the copies and add the
			// powerOfPosset.current().
			DerivedPosset dp = previousPossetsCopy.iterator().next();
			previousPossetsCopy.remove(dp);

			Element e = this.powerOfPossetIterator.current(restriction);

			if (e.isPosset()) {
				throw new UnsupportedOperationException("Power possets of power possets not yet supported!");
			} else {
				Possy p = (Possy) e;

				List<List<Integer>> newValues = findCorrespondingRestrictionValues(dp, p);

				try {
					nextOne = templatePosset.deepPossetClone();
					nextOne.setRestrictionValues(newValues);
				} catch (ChildAlreadyExistsException e1) {
					LOG.error("An error occurred while cloning posset {}", templatePosset, e1);
				}
			}
			this.previousPossets.add(this.currentPosset);
		} else {
			// take powerOfPosset.next() and fill the posset copies
			previousPossetsCopy.clear();
			previousPossetsCopy.add(this.currentPosset);
			previousPossetsCopy.addAll(this.previousPossets);

			DerivedPosset dp = previousPossetsCopy.iterator().next();
			previousPossetsCopy.remove(dp);

			Element e = powerOfPossetIterator.next(restriction, new Parent(this, 0));
			this.powerOfPossetIteratorIndex++;
			if (e.isPosset()) {
				throw new UnsupportedOperationException("Power possets of power possets not yet supported!");
			} else {
				Possy p = (Possy) e;

				List<List<Integer>> newValues = findCorrespondingRestrictionValues(dp, p);

				try {
					nextOne = this.templatePosset.deepPossetClone();
					nextOne.setRestrictionValues(newValues);
				} catch (ChildAlreadyExistsException e1) {
					LOG.error("An error occurred while cloning posset {}", templatePosset, e1);
				}

			}
			this.previousPossets.add(this.currentPosset);
		}
		this.currentPosset = nextOne;
		return nextOne;
	}

	/**
	 * Make sure the list of lists is in the same order as the header variables.
	 * 
	 * @param dp
	 * @param p
	 * @return
	 */
	private List<List<Integer>> findCorrespondingRestrictionValues(DerivedPosset dp, Possy p) {
		List<String> headers = dp.getRestrictionHeader();
		var firstValuesMapping = transformPossyIntoRestrictionValues(templatePosset.getChildren().iterator().next(), p);

		// split valuesMapping into header variables
		var valuesMapping = splitIntoHeaderVariables(headers, firstValuesMapping);

		List<List<Integer>> existingValues = dp.getRestrictionValues();
		for (int i = 0; i < headers.size(); i++) {
			Integer newValue = valuesMapping.get(headers.get(i));
			List<Integer> existingValue = existingValues.get(i);
			List<Integer> copyOfExistingValue = new ArrayList<>(existingValue);
			copyOfExistingValue.add(newValue);
			existingValues.set(i, copyOfExistingValue);
		}
		return existingValues;
	}

	private Map<String, Integer> splitIntoHeaderVariables(List<String> headers, Map<String, Integer> valuesMapping) {
		var newMapping = new HashMap<String, Integer>();
		for (String header : headers) {
			for (Entry<String, Integer> entry : valuesMapping.entrySet()) {
				if (entry.getKey().contains(header)) {
					newMapping.put(header, entry.getValue());
					break;
				}
			}
		}
		return newMapping;
	}

	private Map<String, Integer> transformPossyIntoRestrictionValues(CollapsedVariablesPosset childOfTemplatePosset,
			Element p) {

		assert !p.isPosset();

		Map<String, Integer> values = new HashMap<>();

		if (p instanceof PrimePossy && childOfTemplatePosset instanceof PrimePosset) {
			PrimePossy pp = (PrimePossy) p;
			values.put(childOfTemplatePosset.getVar(), pp.getId());
		} else if (p instanceof DerivedPossy && childOfTemplatePosset instanceof DerivedPosset) {
			DerivedPossy dp = (DerivedPossy) p;

			DerivedPosset childOfTemplate = (DerivedPosset) childOfTemplatePosset;
			DerivedPosset cvp = (DerivedPosset) dp.getGeneratedByPosset();

			// skip some possets in the tree that were added due to collapsedvariables.
			CollapsedVariablesPosset lookPosset = cvp;
			DerivedPosset lookDerivedPosset = cvp;

			if (lookPosset != null && lookPosset instanceof DerivedPosset) {

				lookDerivedPosset = (DerivedPosset) lookPosset;
				Collection<String> childNames = lookDerivedPosset.getChildNames();

				for (String childName : childNames) {

					CollapsedVariablesPosset childPosset = lookDerivedPosset.getChildPosset(childName);

					List<Element> possies = dp.getSubPossies();
					for (int i = 0; i < possies.size(); i++) {
						Element e = possies.get(i);
						if (e.getGeneratedByPosset() == childPosset) {

							var childPossetOfTemplate = childOfTemplate.getChildPosset(childName);

							assert childPossetOfTemplate != null;
							// the powerOf posset has been reconnected, so there is a mismatch.
							values.putAll(transformPossyIntoRestrictionValues(childPossetOfTemplate, e));
						}
					}
				}
			}
		} else {
			LOG.warn("This option should not happen! The possy and possets should align.");
		}
		return values;

	}

	@Override
	public void reset(Parent parentIterator) {

		powerOfPossetIterator.reset(new Parent(this, 0));
		previousPossets.clear();
		currentPosset = null;
		previousPossetsCopy.clear();
	}
}
