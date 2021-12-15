package nl.barry.posset.runtime;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerivedPossy extends Possy {

	private static final Logger LOG = LoggerFactory.getLogger(DerivedPossy.class);

	private List<Element> subPossies;

	public DerivedPossy(Posset aPosset, List<Element> someSubPossies) {
		super(aPosset);
		this.subPossies = new ArrayList<Element>(someSubPossies);
	}

	public List<Element> getSubPossies() {
		return new ArrayList<Element>(this.subPossies);
	}

	@Override
	public String toString() {
		String var = ((CollapsedVariablesPosset) this.generatedByPosset).getVar();
		return (var != null ? var + " " : "") + this.subPossies.toString();
	}

	@Override
	public String toText() {

		StringBuilder sb = new StringBuilder();

		DerivedPosset posset = (DerivedPosset) this.generatedByPosset;
		sb.append("{");
		boolean firstTime = true;
		for (String name : posset.getChildNames()) {
			if (!firstTime)
				sb.append(",");
			else
				firstTime = false;

			Posset child = posset.getChildPosset(name);
			for (Element p : subPossies) {
				if (p.generatedByPosset.equals(child)) {
					sb.append(name).append("=").append(p.toText());
				}
			}
		}

		sb.append("}");

		return sb.toString();
	}

	public Element getSubPossy(String path) {

		String[] split = path.split("\\.");

		if (split.length > 0) {
			Posset subPosset;
			if ((subPosset = ((DerivedPosset) this.generatedByPosset).getChildPosset(split[0])) != null) {
				for (Element p : subPossies) {
					if (p.generatedByPosset == subPosset) {
						if (p instanceof DerivedPossy) {
							if (split.length > 1) {
								return ((DerivedPossy) p).getSubPossy(path.substring(path.indexOf('.') + 1));
							} else {
								return p;
							}

						} else {
							if (split.length > 1) {
								LOG.error("Unexpected PrimePosset, expected more DerivedPossets for path: {}.",
										path.substring(path.indexOf('.') + 1));
								return null;
							} else {
								return p;
							}
						}
					}
				}
			}
		}

		return null;
	}
}
