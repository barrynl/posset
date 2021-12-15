/**
 * 
 */
package nl.barry.posset.runtime;

import java.util.HashSet;
import java.util.Set;

import syntax.ChildAlreadyExistsException;

/**
 * @author Barry
 * 
 */
public abstract class CollapsedVariablesPosset extends Posset {

	/**
	 * In the runtime environment there is always a single or no variable. If
	 * multiple variables are defined at the same posset (by different derived
	 * possets), they get merged when the runtime posset is constructed.
	 */
	private String var;

	/**
	 * Initially the posset structure has only a single parent (is a tree), but
	 * after optimalisation, there might be multiple parents for a single posset
	 * (because all variable occurrences are combined into one).
	 */
	private Set<CollapsedVariablesPosset> parents;

	/**
	 * 
	 * @param aGeneratedByPosset the posset that generated this posset, or null
	 *                           otherwise.
	 */
	public CollapsedVariablesPosset(Posset aGeneratedByPosset) {
		super(aGeneratedByPosset);
		parents = new HashSet<CollapsedVariablesPosset>();
	}

	public CollapsedVariablesPosset() {
		this(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.barry.posset.runtime.Posset#iterator()
	 */
	public abstract CollapsedVariablesIterator<? extends Element> iterator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.barry.posset.runtime.Posset#getName()
	 */
	public abstract String getName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.barry.posset.runtime.Posset#toString()
	 */
	public abstract String toString();

	/**
	 * Checks if the given posset is an ancestor of the current posset. That is: if
	 * the given candidatePosset's name occurs in the hierarchy of this posset. Note
	 * that this check does deliberately not split.
	 * 
	 * @param candidatePosset
	 * @return
	 */
	public abstract boolean hasAncestor(CollapsedVariablesPosset candidatePosset);

	/**
	 * Returns the posset with the same name as candidatePosset.
	 * 
	 * @param candidatePosset The posset whose name to find in this posset.
	 * @return The posset in this hierarchy with the same name as candidatePosset,
	 *         or null if it does not exist.
	 */
	public abstract CollapsedVariablesPosset getAncestor(CollapsedVariablesPosset candidatePosset);

	/**
	 * Get the parent object of the candidate posset if found. Checks if the given
	 * candidate posset exists in the tree of possets of THIS posset. It uses the
	 * NAME of the posset to determine if it exists. So, it starts with
	 * this.getName().equals(candadatePosset.getName()) and goes further down until
	 * it cannot go any further (it stops at any split into two or more possets).
	 * <br />
	 * <br />
	 * It should lie between the variable posset and the candidateposset. We return
	 * null if there is no above ancestor (for example because the common ancestor
	 * is equal to the variable posset).
	 * 
	 * @param candidatePosset
	 * @return
	 */
	public abstract CollapsedVariablesPosset getAboveAncestor(CollapsedVariablesPosset aThis,
			CollapsedVariablesPosset candidatePosset);

	/**
	 * @return the parents
	 */
	public Set<CollapsedVariablesPosset> getParents() {
		return parents;
	}

	/**
	 * Add a parent posset.
	 * 
	 * @param p
	 */
	public void addParent(CollapsedVariablesPosset p) {
		this.parents.add(p);
	}

	/**
	 * Check whether the given posset is a parent of this posset.
	 * 
	 * @param p
	 * @return
	 */
	public boolean hasParent(CollapsedVariablesPosset p) {
		return parents.contains(p);
	}

	/**
	 * Removes the given parent from this possets parents.
	 * 
	 * @param p
	 */
	public void removeParent(CollapsedVariablesPosset p) {
		parents.remove(p);
	}

	/**
	 * @param parents the parent to set
	 */
	public void setParents(Set<CollapsedVariablesPosset> parents) {
		this.parents = parents;
	}

	/**
	 * Get the variable of this posset.
	 * 
	 * @return
	 */
	public String getVar() {
		return var;
	}

	/**
	 * Sets the variable of this posset. Note that there is only a single variable,
	 * because all available variables on this posset are merged into one.
	 * 
	 * @param var
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 * Copies the prime vars to this posset from the corresponding prime vars in the
	 * aPosset.
	 * 
	 * @param aPosset The posset from which to copy the vars.
	 */
	public abstract void copyPrimeVars(CollapsedVariablesPosset aPosset);

	public abstract Set<String> getPrimeVars();

	public abstract CollapsedVariablesPosset deepPossetClone() throws ChildAlreadyExistsException;
}
