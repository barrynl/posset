package nl.barry.posset.runtime;

public class PrimeIterator implements Iterator<Possy> {

	private static final int PRE_START_ID = 0;

	private int currentId = PRE_START_ID;
	private int prime = -1;
	private Posset generatedByPosset;

	public PrimeIterator(Posset aPosset, int aPrime) {
		this.generatedByPosset = aPosset;
		this.prime = aPrime;
	}

	public boolean hasNext() {
		throw new UnsupportedOperationException(
				"This iterator only supports the next() methods which will return NULL when no more possies are available.");
	}

	public Possy next() {
		Possy p = null;
		this.currentId++;
		if (currentId <= prime) {
			p = new PrimePossy(this.getPosset(), this.currentId);
		}

		return p;
	}

	@Override
	public void reset() {
		this.currentId = PRE_START_ID;
	}

	@Override
	public Possy current() {
		Possy p = null;

		if (currentId <= prime) {

			p = new PrimePossy(this.getPosset(), this.currentId);
		}
		return p;
	}

	public Posset getPosset() {
		return this.generatedByPosset;
	}
}
