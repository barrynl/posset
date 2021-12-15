package nl.barry.posset.runtime;

import syntax.ChildAlreadyExistsException;

/**
 * This posset represents the `pick` operator of the posset language that allows
 * the reduction of a powerposset element to a pick and the rest from that
 * posset.
 * 
 * @author nouwtb
 *
 */
public class PickPosset extends DerivedPosset {

	public PickPosset() {
		super(PICK);
	}

	public static final String PICK = "pick";
	public static final String SET = "pos";
	public static final String ELEM = "elem";
	public static final String REST = "rest";

	@Override
	public String getName() {
		return PICK;
	}

	public CollapsedVariablesPosset getPosset() {
		return this.getChildPosset(SET);

	}

	public void setPosset(CollapsedVariablesPosset aPowerPosset) throws ChildAlreadyExistsException {
		this.addChild(SET, aPowerPosset);
	}

	public CollapsedVariablesPosset getElemPosset() {
		return this.getChildPosset(ELEM);
	}

	public void setElemPosset(CollapsedVariablesPosset aElem) throws ChildAlreadyExistsException {
		this.addChild(ELEM, aElem);
	}

	public CollapsedVariablesPosset getRestPosset() {
		return this.getChildPosset(REST);
	}

	public void setRestPosset(CollapsedVariablesPosset aRest) throws ChildAlreadyExistsException {
		this.addChild(REST, aRest);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
