/**
 * 
 */
package nl.barry.posset.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Barry
 * 
 */
public class DerivedPosset extends Posset {
	private static final Logger LOG = LoggerFactory.getLogger(DerivedPosset.class);

	private Posset generatedByPosset = null;
	private Map<String, Posset> subPossets;
	private String name;

	/**
	 * A mapping from the path of node names to a variable nam.
	 */
	private Map<PossetPath, String> variables;

	public DerivedPosset(String aName) {
		super(null);
		this.name = aName;
		this.subPossets = new HashMap<String, Posset>();
	}

	public DerivedPosset(String aName, Posset aGeneratedByPosset) {
		this(aName);
		this.generatedByPosset = aGeneratedByPosset;
	}

	@Override
	public Iterator<? extends Element> iterator() {
		List<Iterator<? extends Element>> iters = new ArrayList<Iterator<? extends Element>>();
		for (Posset p : subPossets.values()) {
			iters.add(p.iterator());
		}
		return new DerivedIterator(this, iters);
	}

	public String getName() {
		return this.name;
	}

	public void addPosset(String aName, Posset aPosset) {
		this.subPossets.put(aName, aPosset);
	}

	public Posset getPosset(String aName) {
		return this.subPossets.get(aName);
	}

	public Set<String> getPossetNames() {
		return this.subPossets.keySet();
	}

	@Override
	public String toString() {
		return "DerivedPosset [subPossets=" + subPossets + ", name=" + name + "]";
	}

	public void addVariable(PossetPath path, String aVar) {
		this.variables.put(path, aVar);
	}

	public Map<PossetPath, String> getVariables() {
		return this.variables;
	}

	@Override
	public Posset getGeneratedByPosset() {
		return null;
	}

	@Override
	public String toText() {
		return "ToTextOfDerivedPosset";
	}
}
