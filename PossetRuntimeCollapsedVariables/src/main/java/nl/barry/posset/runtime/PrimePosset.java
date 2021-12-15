package nl.barry.posset.runtime;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import syntax.ChildAlreadyExistsException;
import syntax.NoPrimeNumberException;

public class PrimePosset extends CollapsedVariablesPosset {

	private static final Logger LOG = LoggerFactory.getLogger(PrimePosset.class);

	private CollapsedVariablesIterator<Possy> iterator;

	@Override
	public String toString() {
		return "'" + prime + "[" + this.getVar() + "](" + Integer.toHexString(this.hashCode()) + ")";
	}

	private int prime;

	public PrimePosset(int aPrime) throws NoPrimeNumberException {
		super();
		this.prime = aPrime;
		if (!isPrime(this.prime)) {
			throw new NoPrimeNumberException("The number '" + this.prime + "' is not a prime number.");
		}
	}

	public String getName() {
		return "'" + this.prime;
	}

	public int getPrime() {
		return this.prime;
	}

	@Override
	public CollapsedVariablesIterator<Possy> iterator() {
		if (this.iterator == null) {
			this.iterator = new PrimeIterator(this, this.prime, this.getVar());
		}
		this.iterator.reset();
		return iterator;
	}

	@Override
	public boolean hasAncestor(CollapsedVariablesPosset candidatePosset) {
		boolean hasIt = false;
		if (this.getName().equals(candidatePosset.getName())) {
			hasIt = true;
		} else if (candidatePosset instanceof AnyPosset) {
			hasIt = true;
		}
		return hasIt;
	}

	@Override
	public CollapsedVariablesPosset getAboveAncestor(CollapsedVariablesPosset aVariablePosset,
			CollapsedVariablesPosset candidatePosset) {

		if (this.getParents().size() > 1) {
			LOG.error("In this phase this posset {} should not have multiple parents.", this);
		}

		if (this.getParents().size() == 1 && !this.getName().equals(aVariablePosset.getName())
				|| candidatePosset instanceof AnyPosset) {
			return this.getParents().iterator().next();
		}
		return null;
	}

	@Override
	public void copyPrimeVars(CollapsedVariablesPosset aPosset) {
		if (aPosset.getVar() != null) {
			if (this.getVar() != null) {
				this.setVar(this.getVar() + "&" + aPosset.getVar());
			} else {
				this.setVar(aPosset.getVar());
			}
		}
	}

	@Override
	public Set<String> getPrimeVars() {
		Set<String> vars = new HashSet<String>();
		vars.add(this.getVar());
		return vars;
	}

	@Override
	public CollapsedVariablesPosset getAncestor(CollapsedVariablesPosset candidatePosset) {
		CollapsedVariablesPosset ancestor = null;
		if (this.getName().equals(candidatePosset.getName()) || candidatePosset instanceof AnyPosset) {
			ancestor = this;
		}
		return ancestor;
	}

	// checks whether an int is prime or not.
	private boolean isPrime(int n) {
		boolean isPrime = true;
		for (int divisor = 2; divisor <= n / 2; divisor++) {
			if (n % divisor == 0) {
				isPrime = false;
				break; // num is not a prime, no reason to continue checking
			}
		}
		return isPrime;
	}

	@Override
	public String toText() {
		return this.toString();
	}

	@Override
	public CollapsedVariablesPosset deepPossetClone() throws ChildAlreadyExistsException {

		PrimePosset pp = null;
		try {
			pp = new PrimePosset(this.getPrime());
			pp.setVar(this.getVar());
		} catch (NoPrimeNumberException e) {
			LOG.error("Could not clone invalid prime posset.", e);
		}
		return pp;
	}
}
