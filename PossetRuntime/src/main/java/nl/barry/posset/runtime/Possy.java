package nl.barry.posset.runtime;

public abstract class Possy extends Element {

	public Possy(Posset aPosset) {
		super(aPosset);
	}

	@Override
	public boolean isPosset() {
		return false;
	}
}
