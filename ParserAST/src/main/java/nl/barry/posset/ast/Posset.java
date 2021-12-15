package nl.barry.posset.ast;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a derived posset. Note that this is part of the Abstract Syntax
 * Tree, so it contains names where you would expect references to other
 * possets.
 * 
 * @author Barry
 * 
 */
public class Posset {

	/**
	 * The name of this relation and this name is used to refer to this particular
	 * posset, so it must be unique.
	 */
	private String name;

	/**
	 * The children of this posset and the root of the dissection tree that defines
	 * the (locations of the) variables bound.
	 */
	private Map<Node, String> children;

	/**
	 * Whether this posset is a power posset or not.
	 */
	private boolean isPower;

	/**
	 * The restriction header. That is, the variables (the order is important) to
	 * which restriction values are attached.
	 */
	private List<String> header;

	/**
	 * A list of restriction values. Their size and order corresponds to the header
	 * list.
	 */
	private List<List<String>> values;

	/**
	 * Construct a new Posset.
	 * 
	 * @param aName
	 */
	public Posset(String aName, boolean aIsPower) {
		this.children = new HashMap<Node, String>();
		this.name = aName;
		this.isPower = aIsPower;

	}

	/**
	 * Add a child to this posset.
	 * 
	 * @param aPosset
	 * @param aRoot
	 * @throws Exception
	 */
	public void addChild(String aPossetName, Node aRoot) throws Exception {
		if (!this.children.containsKey(aRoot))
			this.children.put(aRoot, aPossetName);
		else
			throw new Exception("A child with name '" + aRoot.getName() + "' already exists.");
	}

	/**
	 * A posset can only have one restriction. Which consists of a single header
	 * (which shows the order in which the restriction values are presented) and one
	 * or more restriction values in the order defined by the restriction header.
	 * 
	 * @param aHeader    The header of the restriction with the order and variable
	 *                   names corresponding to the restriction values.
	 * @param someValues The restriction values. Each line consists of values for
	 *                   the PrimePossets corresponding to the header.
	 */
	public void setRestriction(List<String> aHeader, List<List<String>> someValues) {
		this.header = aHeader;

		// check if all header variable names occur in some child node.

		for (String header : this.header) {

			if (!childHasVariable(header, this.children.keySet())) {
				throw new RuntimeException("Header variable '" + header
						+ "' does not occur anywhere in the children of posset '" + this.name + "'");
			}
		}

		this.values = someValues;
	}

	private boolean childHasVariable(String aVar, Collection<Node> someChildren) {
		boolean hasVar = false;
		for (Node child : someChildren) {
			hasVar |= (child.getVar() != null ? child.getVar().equals(aVar) : false)
					|| childHasVariable(aVar, child.getChildren());
		}
		return hasVar;
	}

	/**
	 * REturns the headers if they exist.
	 * 
	 * @return the restriction headers or null otherwise.
	 */
	public List<String> getRestrictionHeader() {
		return this.header;
	}

	/**
	 * Returns the values if they exist.
	 * 
	 * @return The restriction values or null otherwise.
	 */
	public List<List<String>> getRestrictionValues() {
		return this.values;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return A copy of the set of child nodes of this posset.
	 */
	public Set<Node> getChildNodes() {
		Set<Node> nodes = new HashSet<Node>(this.children.keySet());
		return nodes;
	}

	public String getChildType(Node aNode) {
		return this.children.get(aNode);
	}

	/**
	 * Whether this posset is a power posset. I.e. it takes the power of it's single
	 * child.
	 * 
	 * @return
	 */
	public boolean isPowerPosset() {
		return this.isPower;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (this.isPower)
			sb.append("^");

		sb.append(this.name).append("{\n");
		for (Map.Entry<Node, String> entry : this.children.entrySet()) {
			sb.append("\t").append(entry.getValue()).append(" ").append(entry.getKey()).append("\n");
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Posset))
			return false;
		Posset other = (Posset) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
