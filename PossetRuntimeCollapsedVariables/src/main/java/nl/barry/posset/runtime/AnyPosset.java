package nl.barry.posset.runtime;

import java.util.HashSet;
import java.util.Set;

import syntax.ChildAlreadyExistsException;

/**
 * This posset is a wildcard or any posset, that can be used as a placeholder in
 * the PickPosset, to make sure its children get correctly connected into the
 * posset tree.
 * 
 * @author nouwtb
 *
 */
public class AnyPosset extends CollapsedVariablesPosset {

	@Override
	public CollapsedVariablesIterator<? extends Element> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "AnyPosset";
	}

	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * Since we are an AnyPosset, we match with every candidatePosset.
	 */
	@Override
	public boolean hasAncestor(CollapsedVariablesPosset candidatePosset) {
		return true;
	}

	/**
	 * Since we are an AnyPosset, we return ourselves as the matching ancestor.
	 */
	@Override
	public CollapsedVariablesPosset getAncestor(CollapsedVariablesPosset candidatePosset) {
		return this;
	}

	/**
	 * Sicne we are an AnyPosset we always return null. Is this correct?
	 */
	@Override
	public CollapsedVariablesPosset getAboveAncestor(CollapsedVariablesPosset aThis,
			CollapsedVariablesPosset candidatePosset) {

		return null;
	}

	@Override
	public void copyPrimeVars(CollapsedVariablesPosset aPosset) {
		// do nothing
	}

	@Override
	public Set<String> getPrimeVars() {
		return new HashSet<String>();
	}

	@Override
	public CollapsedVariablesPosset deepPossetClone() throws ChildAlreadyExistsException {
		return new AnyPosset();
	}

	@Override
	public String toText() {
		return this.getName();
	}

}
