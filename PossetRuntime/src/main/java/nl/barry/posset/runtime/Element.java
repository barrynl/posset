package nl.barry.posset.runtime;

/**
 * A member of a set.
 * 
 * @author nouwtb
 *
 */
public abstract class Element {

	protected Posset generatedByPosset;

	public Element(Posset aGeneratedByPosset) {
		this.generatedByPosset = aGeneratedByPosset;
	}

	/**
	 * This method can be used to determine whether the Element received from the
	 * {@link Iterator#next()} is either a posset (which can be further iterated) or
	 * a possy (which cannot be further iterated).
	 * 
	 * @return whether this element is a iterable posset or a non-iterable possy.
	 */
	public abstract boolean isPosset();

	public abstract String toString();

	public abstract String toText();

	public Posset getGeneratedByPosset() {
		return this.generatedByPosset;
	}

	public void setGeneratedByPosset(Posset p) {
		this.generatedByPosset = p;
	}

}
