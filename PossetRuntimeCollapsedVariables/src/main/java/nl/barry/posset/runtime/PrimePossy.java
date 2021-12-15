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
		String var = ((CollapsedVariablesPosset)this.generatedByPosset).getVar();
		StringBuilder sb = new StringBuilder();
		(var != null ? sb.append(var).append(":") : sb.append("")).append(id).append("/").append(((PrimePosset) this.generatedByPosset).getPrime());
		return sb.toString();
	}
	
	@Override
	public String toText()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(id).append("/").append(((PrimePosset) this.generatedByPosset).getPrime());
		return sb.toString();
	}
}
