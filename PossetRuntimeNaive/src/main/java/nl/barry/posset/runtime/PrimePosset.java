package nl.barry.posset.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimePosset extends Posset {

	private static final Logger LOG = LoggerFactory.getLogger(PrimePosset.class);

	private Posset generatedByPosset;

	private Iterator<? extends Element> iterator;

	private int prime;

	public PrimePosset(int aPrime) {
		super(null);
		this.prime = aPrime;
	}

	public String getName() {
		return "'" + this.prime;
	}

	public int getPrime() {
		return this.prime;
	}

	@Override
	public Iterator<? extends Element> iterator() {
		if (this.iterator == null) {
			this.iterator = new PrimeIterator(this, this.prime);
		}
		return iterator;
	}

	@Override
	public Posset getGeneratedByPosset() {
		return this.generatedByPosset;
	}

	@Override
	public String toString() {
		return "PrimePosset [generatedByPosset=" + generatedByPosset + ", iterator=" + iterator + ", prime=" + prime
				+ "]";
	}

	@Override
	public String toText() {
		return "toTextOfPrimePosset";
	}

}
