package nl.barry.posset.runtime;

public abstract class Posset extends Element {

	public Posset(Posset aGeneratedByPosset) {
		super(aGeneratedByPosset);
	}

	/**
	 * Give the iterator of this posset.
	 * 
	 * @return
	 */
	public abstract Iterator<? extends Element> iterator();

	/**
	 * The name of the posset.
	 * 
	 * @return
	 */
	public abstract String getName();

	@Override
	public boolean isPosset() {
		return true;
	}

}
