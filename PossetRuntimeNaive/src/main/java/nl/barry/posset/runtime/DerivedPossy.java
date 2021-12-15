package nl.barry.posset.runtime;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerivedPossy extends Possy {

	private static final Logger LOG = LoggerFactory.getLogger(DerivedPossy.class);
	
	private List<Possy> subPossies;

	public DerivedPossy(Posset aPosset, List<Possy> someSubPossies) {
		super(aPosset);
		this.subPossies = new ArrayList<Possy>(someSubPossies);
	}
	
	public List<Possy> getSubPossies()
	{
		return new ArrayList<Possy>(this.subPossies);
	}

	@Override
	public String toString() {
		return subPossies.toString();
	}
	
	public Possy getSubPossy(String path) {

		String[] split = path.split(".");

		if (split.length > 0) {
			Posset subPosset;
			if ((subPosset = ((DerivedPosset) this.generatedByPosset)
					.getPosset(split[0])) != null) {
				for (Possy p : subPossies) {
					if (p.generatedByPosset == subPosset) {
						if (p instanceof DerivedPossy) {
							return ((DerivedPossy) p).getSubPossy(path
									.substring(path.indexOf('.')));
						} else {
							if (split.length > 1) {
								LOG.error(
										"Unexpected PrimePosset, expected more DerivedPossets for path: {}.",
										path.substring(path.indexOf('.')));
							}
							return null;
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public String toText() {
		return this.toString();
	}

}
