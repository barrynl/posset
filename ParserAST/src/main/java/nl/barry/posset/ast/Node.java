package nl.barry.posset.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a tree that dissects the child possets and marks the variable
 * locations.
 * 
 * @author Barry
 * 
 */
public class Node {

	private static final Logger LOG = LoggerFactory.getLogger(Node.class);

	/**
	 * The name of the child posset that this Node represents.
	 */
	private String name;

	/**
	 * An optional variable name that is bound to this Node.
	 */
	private String var;

	/**
	 * The children of this Node forming a tree of Node.
	 */
	private List<Node> children;

	public Node(String aName, String aVar) {
		this.children = new ArrayList<Node>();
		this.name = aName;
		this.var = aVar;
	}

	public void addChild(Node aNode) {
		this.children.add(aNode);
	}

	public void addChildren(Set<Node> childNodes) {
		this.children.addAll(childNodes);
	}

	public String getName() {
		return this.name;
	}

	/**
	 * @return The variable this node has.
	 */
	public String getVar() {
		return this.var;
	}

	public void setVar(String aVarName) {
		this.var = aVarName;
	}

	public List<Node> getChildren() {
		return this.children;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name);

		if (this.var != null)
			sb.append("[").append(this.var).append("]");

		if (this.children.size() == 0) {
			sb.append(";");
		} else if (this.children.size() == 1) {
			sb.append("->").append(this.children.iterator().next());
		} else {
			sb.append(" {");
			for (Node n : this.children) {
				sb.append(n);
			}
			sb.append("}");
		}
		return sb.toString();
	}

	/**
	 * FIrst merge the two trees and then also merge overlapping variables.
	 * 
	 * @param n
	 * @return
	 */
	public Node merge(Node n) {

		Map<String, Set<Node>> mapping = new HashMap<String, Set<Node>>();
		Node n2 = this.merge(n, mapping); // fills the mapping

		if (n2 != null) {
			// merge and add the variables.
			Iterator<String> firstIter = mapping.keySet().iterator();
			Iterator<String> secondIter = mapping.keySet().iterator();

			String firstVar, secondVar, newVar;
			Set<Node> intersectSet, unionSet;
			boolean found;
			while (firstIter.hasNext()) {
				firstVar = firstIter.next();
				found = false;
				while (secondIter.hasNext()) {
					secondVar = secondIter.next();

					if (!firstVar.equals(secondVar)) {
						intersectSet = new HashSet<Node>(mapping.get(firstVar));
						intersectSet.retainAll(mapping.get(secondVar));

						if (!intersectSet.isEmpty()) {
							found = true;
							// overlapping variable found
							newVar = firstVar + "&" + secondVar;

							// create union
							unionSet = new HashSet<Node>(mapping.get(firstVar));
							unionSet.addAll(mapping.get(secondVar));
							for (Node node : unionSet) {
								node.setVar(newVar);
							}
						}
					}
				}
				if (!found) {
					for (Node node : mapping.get(firstVar)) {
						node.setVar(firstVar);
					}
				}
			}
		}
		return n2;
	}

	/**
	 * Copies this Node and prefixes all variables with the given prefix. It leaves
	 * the original Node tree intact. It will be used before calling the merge on
	 * two node trees, to leave the original tree intact (for subsequent queries)
	 * and be able to rename the variables to prevent overlap.
	 * 
	 * @param aVarPrefix
	 * @return
	 */
	public Node copyAndRenameVars(String aVarPrefix) {
		Node newNode = new Node(this.getName(), this.getVar() != null ? aVarPrefix + this.getVar() : null);

		for (Node child : this.getChildren()) {
			newNode.addChild(child.copyAndRenameVars(aVarPrefix));
		}
		return newNode;
	}

	/**
	 * Merges two node trees if possible. Returns null if merging is not possible.
	 * Note that the root of the trees must be equal, otherwise the merge will fail
	 * anyway. Note that only the names of the nodes are used to identify nodes, the
	 * variables may differ.<br />
	 * <br />
	 * This method assumes that there are no overlapping variable names between the
	 * current node and the node with which we want to merge. You can achieve this
	 * by using the Node.copyAndRename() method with a unique prefix per usage of a
	 * posset.
	 * 
	 * @param aNode
	 * @return
	 */
	private Node merge(Node aNode, Map<String, Set<Node>> variableOccurrences) {

		Node newNode = null;
		if (aNode == null) {
			// we merge with nothing.
			// we just copy the rest of the tree and fill the
			// variableOccurrences.
			newNode = new Node(this.getName(), null);
			if (this.getVar() != null) {
				addToCollection(this.getVar(), newNode, variableOccurrences);
			}

			Node n;
			for (Node child : this.getChildren()) {
				n = child.merge(null, variableOccurrences);
				newNode.addChild(n);
			}

		} else if (this.getName().equals(aNode.getName())) {

			// variables will be added later (merged)
			newNode = new Node(this.getName(), null);

			if (this.getVar() != null) {
				addToCollection(this.getVar(), newNode, variableOccurrences);
			}

			if (aNode.getVar() != null) {
				addToCollection(aNode.getVar(), newNode, variableOccurrences);
			}

			List<Node> theseChildren = this.getChildren();
			List<Node> thoseChildren = aNode.getChildren();

			Node n;
			for (Node thisNode : theseChildren) {
				if (thoseChildren.contains(thisNode)) {
					// find the other node and merge them
					for (Node thatNode : thoseChildren) {
						if ((n = thisNode.merge(thatNode, variableOccurrences)) != null) {
							newNode.addChild(n);
						}
					}

				} else {
					n = thisNode.merge(null, variableOccurrences);
					newNode.addChild(n);
				}
			}

			for (Node thatNode : aNode.getChildren()) {
				if (!theseChildren.contains(thatNode)) {
					n = thatNode.merge(null, variableOccurrences);
					newNode.addChild(n);
				}
			}
		}
		return newNode;
	}

	private void addToCollection(String aVar, Node aNode, Map<String, Set<Node>> someVariableOccurrences) {
		if (someVariableOccurrences.containsKey(aVar)) {
			Set<Node> occurrences = someVariableOccurrences.get(aVar);

			if (!occurrences.contains(aNode)) {
				occurrences.add(aNode);
			} else {
				LOG.error("The node called {} should not be already registered for var {}.", aNode.getName(), aVar);
			}
		} else {

			Set<Node> newSet = new HashSet<Node>();
			newSet.add(aNode);
			someVariableOccurrences.put(aVar, newSet);
		}
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
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}