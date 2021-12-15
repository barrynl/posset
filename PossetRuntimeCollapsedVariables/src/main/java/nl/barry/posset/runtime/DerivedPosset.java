package nl.barry.posset.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import syntax.ChildAlreadyExistsException;

public class DerivedPosset extends CollapsedVariablesPosset {

	private static final Logger LOG = LoggerFactory.getLogger(DerivedPosset.class);

	/**
	 * A mapping from LOCAL (!) names to the posset of which this derived posset is
	 * made. TODO Shouldn't this be an ordered mapping, since we are creating a list
	 * from it for the iterator?
	 */
	private Map<String, CollapsedVariablesPosset> childPossets;

	/**
	 * A list of variables.
	 */
	private List<String> restrictionHeader;

	/**
	 * A list of values per variable.
	 */
	private List<List<Integer>> restrictionValues;

	/**
	 * The name of this derived posset. This is the global name within the program.
	 */
	private String name;

	private CollapsedVariablesIterator<? extends Element> iterator;

	public DerivedPosset(Posset aGeneratedByPosset, String aName, Map<String, CollapsedVariablesPosset> somePossets) {
		super(aGeneratedByPosset);
		this.name = aName;

		for (CollapsedVariablesPosset posset : somePossets.values()) {
			posset.addParent(this);
		}

		this.childPossets = somePossets;
	}

	public DerivedPosset(String aName) {
		this(null, aName, new HashMap<String, CollapsedVariablesPosset>());
	}

	public void addChild(String aName, CollapsedVariablesPosset p) throws ChildAlreadyExistsException {

		if (childPossets.containsKey(aName)) {
			throw new ChildAlreadyExistsException("Posset " + this.getName() + " already contains child name " + aName);
		}

		childPossets.put(aName, p);
		p.addParent(this);
	}

	public void addChildWithoutParent(String aName, CollapsedVariablesPosset p) throws ChildAlreadyExistsException {

		if (childPossets.containsKey(aName)) {
			throw new ChildAlreadyExistsException("Posset " + this.getName() + " already contains child name " + aName);
		}

		childPossets.put(aName, p);
	}

	public void setChild(String aName, CollapsedVariablesPosset p) {
		if (childPossets.containsKey(aName)) {
			childPossets.put(aName, p);
			p.addParent(this);
		} else {
			LOG.error("DerivedPosset.setChild() should only be used to set an existing child. {} does not exist.",
					aName);
		}
	}

	public Collection<String> getChildNames() {
		return childPossets.keySet();
	}

	public Collection<CollapsedVariablesPosset> getChildren() {
		return childPossets.values();
	}

	public CollapsedVariablesPosset getChildPosset(String aName) {
		return this.childPossets.get(aName);
	}

	private boolean containsOneOfVars(List<String> restriction, String aVar) {
		boolean contains = false;
		String[] vars = aVar.split("&");

		for (String var : vars) {
			contains |= restriction.contains(var);
		}
		return contains;
	}

	/**
	 * We only allow a single iterator per object. So that iterators are shared
	 * between possets that have the same variable.
	 */
	@Override
	public CollapsedVariablesIterator<? extends Element> iterator() {
		if (this.iterator == null) {
			List<CollapsedVariablesIterator<? extends Element>> iters = new ArrayList<CollapsedVariablesIterator<? extends Element>>(
					childPossets.size());
			List<String> names = new ArrayList<String>(childPossets.size());

			for (Map.Entry<String, CollapsedVariablesPosset> entry : childPossets.entrySet()) {
				names.add(entry.getKey());
			}
			Collections.sort(names);

			for (String name : names) {
				iters.add(childPossets.get(name).iterator());
			}

			if (this.restrictionHeader != null && this.restrictionValues != null) {
				Map<String, List<Integer>> aRestriction = this.getRestriction();

				this.iterator = new DerivedIterator(this, names, iters, aRestriction);
			} else {
				// no full restriction defined.
				this.iterator = new DerivedIterator(this, names, iters);
			}

		}
		this.iterator.reset();
		return this.iterator;
	}

	/**
	 * Convert the restriction header/values into the correct format for the
	 * iterator.
	 * 
	 * @return
	 */
	private Map<String, List<Integer>> getRestriction() {

		Map<String, List<Integer>> aRestriction = new HashMap<String, List<Integer>>();
		for (int i = 0; i < this.restrictionHeader.size(); i++) {
			String var = this.restrictionHeader.get(i);
			aRestriction.put(var, this.restrictionValues.get(i));
		}

		return aRestriction;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setRestrictionHeader(List<String> aHeader) {
		this.restrictionHeader = aHeader;
	}

	/**
	 * @return a copy of the restriction header list.
	 */
	public List<String> getRestrictionHeader() {
		return this.restrictionHeader != null ? new ArrayList<String>(this.restrictionHeader) : null;
	}

	public void setRestrictionValues(List<List<Integer>> someValues) {
		this.restrictionValues = someValues;
	}

	/**
	 * 
	 * @return A copy of the restriction values list of lists.
	 */
	public List<List<Integer>> getRestrictionValues() {
		return restrictionValues != null ? new ArrayList<List<Integer>>(this.restrictionValues) : null;
	}

	@Override
	public String toString() {
		return name + "#" + this.restrictionHeader + "|" + this.getVar() + "|(" + Integer.toHexString(this.hashCode())
				+ ")" + childPossets;
	}

	@Override
	public boolean hasAncestor(CollapsedVariablesPosset candidatePosset) {

		boolean isAncestor = false;

		if (this.getName().equals(candidatePosset.getName())) {
			isAncestor = true;
		} else if (this.getChildNames().size() == 1) {
			isAncestor = this.getChildPosset(this.getChildNames().iterator().next()).hasAncestor(candidatePosset);
		} else if (candidatePosset instanceof AnyPosset) {
			isAncestor = true;
		}

		return isAncestor;
	}

	@Override
	public CollapsedVariablesPosset getAboveAncestor(CollapsedVariablesPosset aVariablePosset,
			CollapsedVariablesPosset ancestorPosset) {
		CollapsedVariablesPosset p = null;

		// we want to return null if the posset on which the variable exists IS
		// the ancestorposset.
		if (this.getName().equals(ancestorPosset.getName()) || ancestorPosset instanceof AnyPosset) {

			if (this != aVariablePosset) {

				if (this.getParents().size() == 1) {
					p = this.getParents().iterator().next();
				} else {
					throw new IllegalStateException("At this point a posset should not have multiple parents yet.");
				}
			}
		} else if (this.getChildNames().size() == 1) {
			CollapsedVariablesPosset p2 = this.getChildPosset(this.getChildNames().iterator().next());
			p = p2.getAboveAncestor(aVariablePosset, ancestorPosset);
		}
		return p;
	}

	@Override
	public void copyPrimeVars(CollapsedVariablesPosset aPosset) {

		DerivedPosset dp = null;
		CollapsedVariablesPosset cp = null;
		for (Map.Entry<String, CollapsedVariablesPosset> child : this.childPossets.entrySet()) {
			if (aPosset instanceof DerivedPosset) {
				dp = (DerivedPosset) aPosset;
				cp = dp.getChildPosset(child.getKey());
				child.getValue().copyPrimeVars(cp);
			} else {
				LOG.error(
						"In {} DerivedPosset expected but found {} for child {}. CopyPrimeVars() should only be called on two corresponding possets.",
						new Object[] { this, aPosset, child.getKey() });
			}
		}
	}

	@Override
	public Set<String> getPrimeVars() {
		Set<String> primeVars = new HashSet<String>();
		for (CollapsedVariablesPosset child : this.childPossets.values()) {
			primeVars.addAll(child.getPrimeVars());
		}
		return primeVars;
	}

	@Override
	public CollapsedVariablesPosset getAncestor(CollapsedVariablesPosset candidatePosset) {
		CollapsedVariablesPosset ancestor = null;

		if (this.getName().equals(candidatePosset.getName()) || candidatePosset instanceof AnyPosset) {
			ancestor = this;
		} else if (this.getChildNames().size() == 1) {
			ancestor = this.getChildPosset(this.getChildNames().iterator().next()).getAncestor(candidatePosset);
		}

		return ancestor;
	}

	@Override
	public String toText() {

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		var iter = this.iterator();
		boolean firstTime = true;
		while (iter.hasNext()) {
			if (!firstTime)
				sb.append(",");
			else
				firstTime = false;
			sb.append(iter.next());
		}
		sb.append("}");

		return sb.toString();
	}

	/**
	 * Note that also the parent references are cloned, so if they should be
	 * different than the original you should set them after the clone.
	 */
	@Override
	public DerivedPosset deepPossetClone() throws ChildAlreadyExistsException {
		DerivedPosset theClone = new DerivedPosset(this.getName());
//		theClone.setParents(new HashSet<>(this.getParents()));
		theClone.setGeneratedByPosset(this.getGeneratedByPosset());

		for (String cvPossetName : this.getChildNames()) {

			CollapsedVariablesPosset toClone = this.getChildPosset(cvPossetName);
			CollapsedVariablesPosset cloned = toClone.deepPossetClone();

			// the parents are no longer the parents of the
			// clone. We have to set them in the derived posset deepPossetClone()
			cloned.getParents().clear();
			theClone.addChild(cvPossetName, cloned);
		}

		theClone.setRestrictionHeader(this.getRestrictionHeader());
		theClone.setRestrictionValues(this.getRestrictionValues());

		theClone.setVar(this.getVar());

		return theClone;
	}

	public DerivedPosset shallowPossetClone() throws ChildAlreadyExistsException {
		DerivedPosset theClone = new DerivedPosset(this.getName());
		theClone.setParents(this.getParents());
		for (String cvPossetName : this.getChildNames()) {
			theClone.addChild(cvPossetName, this.getChildPosset(cvPossetName));
		}

		theClone.setRestrictionHeader(this.getRestrictionHeader());
		theClone.setRestrictionValues(this.getRestrictionValues());

		theClone.setVar(this.getVar());

		return theClone;
	}
}
