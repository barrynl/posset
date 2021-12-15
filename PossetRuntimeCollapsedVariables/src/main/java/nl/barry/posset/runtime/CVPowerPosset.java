package nl.barry.posset.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import syntax.ChildAlreadyExistsException;

public class CVPowerPosset extends CollapsedVariablesPosset {

	private static final Logger LOG = LoggerFactory.getLogger(CVPowerPosset.class);

	private CollapsedVariablesPosset generatedByPosset = null;
	private String name = null;
	private DerivedPosset templatePosset = null;
	private CollapsedVariablesPosset powerOf = null;

	/**
	 * A list of variables.
	 */
	private List<String> restrictionHeader;

	/**
	 * A list of values per variable.
	 */
	private List<List<Integer>> restrictionValues;

	private CollapsedVariablesIterator<CollapsedVariablesPosset> iterator;

	public CVPowerPosset(String aName, CollapsedVariablesPosset aGeneratedByPosset,
			CollapsedVariablesPosset aPowerOfPosset, DerivedPosset aTemplatePosset) {
		super(aGeneratedByPosset);
		this.name = aName;
		this.powerOf = aPowerOfPosset;
		this.templatePosset = aTemplatePosset;
	}

	public CVPowerPosset(String aName) {
		super(null);
		this.name = aName;
	}

	@Override
	public Posset getGeneratedByPosset() {
		return this.generatedByPosset;
	}

	@Override
	public CollapsedVariablesIterator<CollapsedVariablesPosset> iterator() {

		if (this.iterator == null) {
			// create iterator
			this.iterator = new PowerIterator(this, (CollapsedVariablesPosset) this.powerOf, this.templatePosset);
		}

		return this.iterator;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toText() {
		return this.toString();
	}

	public void setPowerOfPosset(CollapsedVariablesPosset aPowerOfPosset) {
		this.powerOf = aPowerOfPosset;

	}

	public CollapsedVariablesPosset getPowerOfPosset() {
		return this.powerOf;
	}

	public void setTemplatePosset(DerivedPosset aTemplatePosset) {
		this.templatePosset = aTemplatePosset;

	}

	public DerivedPosset getTemplatePosset() {
		return this.templatePosset;
	}

	@Override
	public boolean hasAncestor(CollapsedVariablesPosset candidatePosset) {

		boolean hasIt = false;
		if (candidatePosset.getName().equals(this.getName())) {
			hasIt = true;
		} else if (candidatePosset instanceof AnyPosset) {
			hasIt = true;
		} else {
		}
		return hasIt;
	}

	@Override
	public CollapsedVariablesPosset getAncestor(CollapsedVariablesPosset candidatePosset) {
		if (candidatePosset.getName().equals(this.getName()) || candidatePosset instanceof AnyPosset) {
			return this;
		}
		return null;
	}

	/**
	 * The {@code VariablePosset} and {@code this} start out as the same, but then
	 * we dive into the tree and aVariablePosset will remain the same while
	 * {@code this} will change.
	 */
	@Override
	public CollapsedVariablesPosset getAboveAncestor(CollapsedVariablesPosset aVariablePosset,
			CollapsedVariablesPosset anAncestorPosset) {

		CollapsedVariablesPosset aboveAncestor = null;

		if (this.getName().equals(anAncestorPosset.getName()) || anAncestorPosset instanceof AnyPosset) {
			if (this != aVariablePosset) {
				if (this.getParents().size() == 1) {
					aboveAncestor = this.getParents().iterator().next();
				} else {
					LOG.error("At this point a posset should not have multiple parents yet.");
				}
			}
		}
		return aboveAncestor;
	}

	@Override
	public void copyPrimeVars(CollapsedVariablesPosset aPosset) {

		if (aPosset instanceof CVPowerPosset) {
			CVPowerPosset otherPP = (CVPowerPosset) aPosset;
			var otherPowerOf = otherPP.getPowerOfPosset();
			this.powerOf.copyPrimeVars(otherPowerOf);
		} else {
			LOG.error("copyPrimeVars should only be called on corresponding possets and not on {} and {}.",
					aPosset.getClass().getSimpleName(), this.getClass().getSimpleName());
		}
	}

	@Override
	public Set<String> getPrimeVars() {
		Set<String> primeVars = new HashSet<String>();
		primeVars.addAll(this.templatePosset.getPrimeVars());
		return primeVars;
	}

	@Override
	public CVPowerPosset deepPossetClone() throws ChildAlreadyExistsException {
		CVPowerPosset theClone = new CVPowerPosset(this.getName());
		theClone.setParents(this.getParents());
		theClone.setTemplatePosset(this.getTemplatePosset().deepPossetClone());
		theClone.setPowerOfPosset(this.getPowerOfPosset().deepPossetClone());
		theClone.setVar(this.getVar());
		return theClone;

	}

	@Override
	public String toString() {
		return "CVPowerPosset [generatedByPosset=" + generatedByPosset + ", name=" + name + ", templatePosset="
				+ templatePosset + ", powerOf=" + powerOf + ", restrictionHeader=" + restrictionHeader
				+ ", restrictionValues=" + restrictionValues + "]";
	}

}
