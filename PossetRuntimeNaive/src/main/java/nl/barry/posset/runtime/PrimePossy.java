package nl.barry.posset.runtime;

public class PrimePossy extends Possy {
	
	private int id;
	
	public PrimePossy(Posset aPosset, int anId)
	{
		super(aPosset);
		this.id = anId;
	}
	
	public int getId()
	{
		return this.id;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id).append("/").append(((PrimePosset) this.generatedByPosset).getPrime());
		return sb.toString();
	}

	@Override
	public String toText() {
		return this.toString();
	}
}
