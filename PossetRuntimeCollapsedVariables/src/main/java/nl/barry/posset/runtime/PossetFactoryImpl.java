/**
 * 
 */
package nl.barry.posset.runtime;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.barry.posset.ast.Node;
import syntax.ChildAlreadyExistsException;
import syntax.ChildDoesNotExistException;
import syntax.IncompatibleRestrictionValuesException;
import syntax.IncompatibleVariableTargetsException;
import syntax.InvalidRestrictionVariableLocationException;
import syntax.OnlySingleChildAllowedException;
import syntax.PossetNotFoundException;
import syntax.SyntaxException;

/**
 * @author Barry
 * 
 */
public class PossetFactoryImpl implements PossetFactory {

	private static final String TEMPLATE_VAR = "$TV";

	private static final Logger LOG = LoggerFactory.getLogger(PossetFactoryImpl.class);

	/**
	 * All variables X in a DerivedPosset definition A need to be distinguishable
	 * from all variables X in DerivedPosset definition B. To achieve this, we need
	 * a global variable prefix that increases for each call to getRuntimePosset()
	 */
	private int variablePrefix = 1;

	public int getUniqueVariablePrefix() {
		int prefix = variablePrefix;
		this.variablePrefix++;
		return prefix;
	}

	/**
	 * Returns the runtime posset with name {@code name} using the abstract syntax
	 * tree objects for building it.
	 */
	public CollapsedVariablesPosset getRuntimePosset(Map<String, nl.barry.posset.ast.Posset> nameMapping, String name)
			throws SyntaxException {
		Map<Set<String>, Set<CollapsedVariablesPosset>> variableMapping = new HashMap<Set<String>, Set<CollapsedVariablesPosset>>();
		CollapsedVariablesPosset pos = this.getRuntimePosset(nameMapping, name, new LinkedList<Node>(),
				variableMapping);

		LOG.trace("Main BEFORE collapsing variables.");
		logPossetHierarchy(pos, 0);

		logVariableMapping(variableMapping);
		// now find the overlapping variables.
		setOverlappingVariables(variableMapping);

		LOG.trace("Main AFTER  collapsing variables");
		logPossetHierarchy(pos, 0);

		if (pos instanceof DerivedPosset) {
			DerivedPosset dp = (DerivedPosset) pos;
			// check if variables of restriction headers are on prime possets.
			// we cannot check it on the AST because we do not know the types of the nodes
			// yet, but we cannot check it on the RuntimePossets either, because the
			// variables are all mixed up? No, maybe they are not.
			checkRestrictionHeadersLocations(dp, new ArrayList<String>());
		}

		return pos;
	}

	private void checkRestrictionHeadersLocations(DerivedPosset pos, List<String> restrictionHeaderVars)
			throws SyntaxException {

		List<String> headerVars = pos.getRestrictionHeader();
		if (headerVars != null) {
			restrictionHeaderVars.addAll(headerVars);
			for (String headerVar : restrictionHeaderVars) {
				for (CollapsedVariablesPosset childPosset : pos.getChildren()) {
					String var = childPosset.getVar();

					if (var != null) {
						String[] splittedVar = var.split("&");

						for (String splitVar : splittedVar) {
							if (headerVar.equals(splitVar)) {
								if (!(childPosset instanceof PrimePosset)) {
									throw new InvalidRestrictionVariableLocationException(
											"The header variable '" + headerVar + "' of posset '" + pos.getName()
													+ "' is not located on a prime posset, but on '"
													+ childPosset.getName() + "'");
								}
							}
						}
					}
				}
			}
		}

		for (CollapsedVariablesPosset childPos : pos.getChildren()) {
			if (childPos instanceof DerivedPosset) {
				DerivedPosset cvPos = (DerivedPosset) childPos;
				checkRestrictionHeadersLocations(cvPos, restrictionHeaderVars);
			}
		}
	}

	/**
	 * @param variableMapping
	 */
	private void logVariableMapping(Map<Set<String>, Set<CollapsedVariablesPosset>> variableMapping) {
		LOG.debug("Non-restriction VariableMappings (total {}): ", variableMapping.size());
		for (Set<String> varName : variableMapping.keySet()) {
			Set<CollapsedVariablesPosset> posset = variableMapping.get(varName);
			if (posset.size() > 1) {
				LOG.debug("{}", varName);

				for (CollapsedVariablesPosset p2 : posset) {
					LOG.debug("\t{}", getPath(p2));
				}
			}
		}
	}

	private void logPossetHierarchy(CollapsedVariablesPosset posset, int indent) {

		String name = posset.getName();
		StringBuilder tabs = new StringBuilder();
		for (int i = 0; i < indent; i++)
			tabs.append('\t');
		String var = posset.getVar() != null ? "[" + posset.getVar() + "]" : "";
		String hash = Integer.toHexString(posset.hashCode());
		LOG.debug("{}{}{}({})", tabs, name, var, hash);

		if (posset instanceof DerivedPosset) {
			var dp = (DerivedPosset) posset;
			for (CollapsedVariablesPosset child : dp.getChildren()) {
				logPossetHierarchy(child, indent + 1);
			}
		} else if (posset instanceof CVPowerPosset) {
			var pp = (CVPowerPosset) posset;
			logPossetHierarchy(pp.getPowerOfPosset(), indent + 1);
		} else if (posset instanceof PickPosset) {
			var pp = (PickPosset) posset;
			logPossetHierarchy(pp.getPosset(), indent + 1);
			logPossetHierarchy(pp.getElemPosset(), indent + 1);
			logPossetHierarchy(pp.getRestPosset(), indent + 1);
		} else if (posset instanceof PrimePosset) {
			// not further children.
		}

	}

	/**
	 * @param aPosset
	 * @return The path from the Root to the given Posset. Note that in case of
	 *         multiple path from root to given Posset, it will choose one
	 *         arbitrarily.
	 */
	private String getPath(CollapsedVariablesPosset aPosset) {
		StringBuilder sb = new StringBuilder();
		CollapsedVariablesPosset parent = aPosset;
		do {
			sb.insert(0, parent.getName() + "(" + Integer.toHexString(parent.hashCode()) + ")");
			sb.insert(0, " / ");

			Iterator<CollapsedVariablesPosset> iter = parent.getParents().iterator();

			if (iter.hasNext()) {
				parent = iter.next();
			} else {
				parent = null;
			}
		} while (parent != null);
		return sb.toString();
	}

	/**
	 * Converts the Abstract Syntax Tree posset to a runtime posset that can
	 * actually generate possibilities.
	 * 
	 * @param possetByName    A mapping from name to AST posset.
	 * @param aPossetName     The name of the posset we are currently transforming
	 *                        into a runtime posset.
	 * @param currentNodes    The list of defined nodes at the current level in the
	 *                        posset. It is, among other things, used for building
	 *                        the variable mapping (that is needed for collapsing).
	 *                        It is also used to match the child nodes of a usage of
	 *                        a posset to the definition of a posset.
	 * @param variableMapping A mapping from variables names to the possets they are
	 *                        attached to.
	 * @return The corresponding runtime posset, based on the AST posset.
	 * @throws SyntaxException                        If a probem occurs while
	 *                                                traversing the AST.
	 * @throws IncompatibleRestrictionValuesException If there are more or less
	 *                                                restriction values than the
	 *                                                number of restriction headers.
	 */
	private CollapsedVariablesPosset getRuntimePosset(Map<String, nl.barry.posset.ast.Posset> possetByName,
			String aPossetName, LinkedList<Node> currentNodes,
			Map<Set<String>, Set<CollapsedVariablesPosset>> variableMapping) throws SyntaxException {
		LOG.debug("Building {} with overlay nodes {}", aPossetName, currentNodes);

		/*
		 * Retrieve a unique variable prefix to rename variables.
		 */
		int variablePrefix = this.getUniqueVariablePrefix();

		nl.barry.posset.ast.Posset aPosset = possetByName.get(aPossetName);

		CollapsedVariablesPosset pos = null;

		if (aPosset != null) {

			if (aPosset.isPowerPosset()) {
				LOG.trace("PowerPosset found: ^{}", aPosset.getName());
				// if a power posset, then we neeed to create a posset with a single powerOf
				// posset.
				CVPowerPosset p = new CVPowerPosset(aPosset.getName());

				Set<Node> children = aPosset.getChildNodes();

				if (children.isEmpty() || children.size() > 1) {
					throw new OnlySingleChildAllowedException("PowerPosset '" + aPosset.getName()
							+ "' can only have a single child and not '" + children.size() + "'.");
				}

				// first store all variables in the variableMapping
				for (Node n : currentNodes) {
					if (n.getVar() != null) {
						addToCollection(n.getVar(), p, variableMapping);
					}
				}

				Node childNode = children.iterator().next();

				// do I need to copy the current nodes? Probably, but do I have to add something
				// as well?
				LinkedList<Node> currentNodesCopy = new LinkedList<Node>();
				currentNodesCopy.clear();
				currentNodesCopy.addAll(currentNodes);

				updateCurrentNodes(aPosset, currentNodesCopy, childNode);

				CollapsedVariablesPosset powerOfPosset = getRuntimePosset(possetByName, aPosset.getChildType(childNode),
						currentNodesCopy, variableMapping);

				CollapsedVariablesPosset clonedPosset = getRuntimePosset(possetByName, aPosset.getChildType(childNode),
						currentNodesCopy, variableMapping);

				powerOfPosset.addParent(p);

				// create template posset from derivedPossetChild.
				DerivedPosset templatePosset = new DerivedPosset("templateOf-" + powerOfPosset.getName());

				// Make sure the children of the clonedPosset's are also present in variables
				// mapping.
				templatePosset.addChild("copyOfPowerOfPosset", clonedPosset);
				templatePosset.setGeneratedByPosset(p);
				ArrayList<String> variables = new ArrayList<String>();
				setRestrictionVariables(templatePosset, variables, 0);
				templatePosset.setRestrictionHeader(variables);
				ArrayList<List<Integer>> emptyValues = new ArrayList<List<Integer>>();
				for (int i = 0; i < variables.size(); i++) {
					emptyValues.add(new ArrayList<Integer>());
				}
				templatePosset.setRestrictionValues(emptyValues);

				p.setPowerOfPosset(powerOfPosset);
				p.setTemplatePosset(templatePosset);

				pos = p;
			} else {

				DerivedPosset p = new DerivedPosset(aPossetName);

				// add and convert the restriction
				List<String> restrictionHeader = aPosset.getRestrictionHeader();
				List<List<String>> restrictionValues = aPosset.getRestrictionValues();

				if (restrictionHeader != null && restrictionValues != null) {

					List<String> newRestrictionHeader = new ArrayList<String>();
					for (String oldVar : restrictionHeader) {
						newRestrictionHeader.add(variablePrefix + oldVar);
					}

					p.setRestrictionHeader(newRestrictionHeader);
					List<List<Integer>> newValues = new ArrayList<List<Integer>>();

					// one list for every variable.
					for (String var : aPosset.getRestrictionHeader()) {
						newValues.add(new ArrayList<Integer>());
					}
					Integer i;

					for (List<String> toConvert : restrictionValues) {

						if (toConvert.size() != restrictionHeader.size()) {
							String message = MessageFormat.format(
									"The posset {0} has incompatible number of restriction values {1}", aPossetName,
									toConvert);
							throw new IncompatibleRestrictionValuesException(message);
						}

						for (int j = 0; j < toConvert.size(); j++) {
							String text = toConvert.get(j);
							try {
								i = Integer.parseInt(text.substring(1));
								newValues.get(j).add(i);
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace();
							}
						}
					}
					p.setRestrictionValues(newValues);
				}
				String type;
				CollapsedVariablesPosset derivedPossetChild;

				// first store all variables in the variableMapping
				for (Node n : currentNodes) {
					if (n.getVar() != null) {
						addToCollection(n.getVar(), p, variableMapping);
					}
				}

				LinkedList<Node> currentNodesCopy = new LinkedList<Node>();

				// then process the children of the current posset.
				for (Node child : aPosset.getChildNodes()) {

					currentNodesCopy.clear();
					currentNodesCopy.addAll(currentNodes);

					type = aPosset.getChildType(child);

					String nodeName = child.getName();

//					LOG.trace("Found node name {}", nodeName);

					// update the current nodes
					updateCurrentNodes(aPosset, currentNodesCopy, child);

					// add to current nodes
					Node childCopy = child.copyAndRenameVars(Integer.toString(variablePrefix));
					currentNodesCopy.add(childCopy);

					derivedPossetChild = getRuntimePosset(possetByName, type, currentNodesCopy, variableMapping);

					// so, you might get back the currentNodes list differently
					// than you send it, make sure this does not fail!
					p.addChild(nodeName, derivedPossetChild);
				}

				pos = p;
			}
		} else if (aPossetName.equals(PickPosset.PICK)) {

			// Found the pick operator! We expect three children: posset, elem and rest.
			PickPosset pp = new PickPosset();
			AnyPosset aPowerPosset = new AnyPosset();
			AnyPosset aElem = new AnyPosset();
			AnyPosset aRest = new AnyPosset();

			for (Node n : currentNodes.iterator().next().getChildren()) {
				if (n.getName().equals(PickPosset.SET) && n.getVar() != null) {

					addToCollection(n.getVar(), aPowerPosset, variableMapping);

//					variableMapping.put(new HashSet<String>(Arrays.asList(n.getVar())),
//							new HashSet<CollapsedVariablesPosset>(Arrays.asList(aPowerPosset)));
				} else if (n.getName().equals(PickPosset.ELEM) && n.getVar() != null) {

					addToCollection(n.getVar(), aElem, variableMapping);

//					variableMapping.put(new HashSet<String>(Arrays.asList(n.getVar())),
//							new HashSet<CollapsedVariablesPosset>(Arrays.asList(aElem)));
				} else if (n.getName().equals(PickPosset.REST) && n.getVar() != null) {
					addToCollection(n.getVar(), aRest, variableMapping);

//					variableMapping.put(new HashSet<String>(Arrays.asList(n.getVar())),
//							new HashSet<CollapsedVariablesPosset>(Arrays.asList(aRest)));
				}
			}

			pp.setPosset(aPowerPosset);
			pp.setElemPosset(aElem);
			pp.setRestPosset(aRest);

			pos = pp;

		} else {
			// it is a prime posset
			try {
				pos = new PrimePosset(Integer.parseInt(aPossetName.substring(1)));

				// store all variables in the variableMapping
				for (Node n : currentNodes) {
					if (n.getVar() != null) {
						addToCollection(n.getVar(), pos, variableMapping);
					}
				}
			} catch (NumberFormatException pe) {
				throw new PossetNotFoundException(
						"Not a prime posset and a posset called '" + aPossetName + "' cannot be found.", pe);
			}
		}
		return pos;
	}

	/**
	 * 
	 * @param aPosset
	 * @param currentNodesCopy
	 * @param child
	 * @throws ChildDoesNotExistException
	 */
	private void updateCurrentNodes(nl.barry.posset.ast.Posset aPosset, LinkedList<Node> currentNodesCopy, Node child)
			throws ChildDoesNotExistException {
		ListIterator<Node> nodeIter = currentNodesCopy.listIterator();
		boolean hasChild;
		Node newChild;
		while (nodeIter.hasNext()) {
			newChild = null;
			hasChild = false;
			Node n = nodeIter.next();

			// check if n has a child named 'nodeName'
			for (Node aChild : n.getChildren()) {
				if (aChild.getName().equals(child.getName())) {
					// in this posset,
					hasChild = true;
					newChild = aChild;
				} else {
					// otherwise throw ChildDOesNotExistException
					boolean childExists = false;
					for (Node o : aPosset.getChildNodes()) {
						if (o.getName().equals(aChild.getName())) {
							childExists = true;
						}
					}

					if (!childExists) {
						throw new ChildDoesNotExistException(
								"Posset " + aPosset.getName() + " should have a child named " + aChild.getName() + ".");
					}

				}
			}

			// if so, replace this node by its child.
			// if not, remove the entry from the list.
			if (hasChild) {
				nodeIter.set(newChild);
			} else {
				nodeIter.remove();
			}
		}
	}

	/**
	 * Adds a unique variable to each prime posset of the given posset.
	 * 
	 * @param templatePosset The posset who's primepossets need to have a variable.
	 * @param variables      The list that will be filled with variable names added.
	 * @param varIndex       The variable index to be used for naming.
	 * @return the variable index after it is being used for naming.
	 */
	private int setRestrictionVariables(Posset templatePosset, List<String> variables, int varIndex) {
		if (templatePosset instanceof PrimePosset) {
			PrimePosset p = (PrimePosset) templatePosset;
			String varName = TEMPLATE_VAR + varIndex;
			p.setVar(varName);
			variables.add(varName);
			varIndex += 1;
		} else if (templatePosset instanceof DerivedPosset) {
			DerivedPosset dp = (DerivedPosset) templatePosset;

			Collection<CollapsedVariablesPosset> children = dp.getChildren();

			for (CollapsedVariablesPosset cvp : children) {
				varIndex = setRestrictionVariables(cvp, variables, varIndex);
			}

		}

		return varIndex;
	}

	private void addToCollection(String aVar, CollapsedVariablesPosset aPosset,
			Map<Set<String>, Set<CollapsedVariablesPosset>> someVariableOccurrences) {

		Set<String> set = new HashSet<String>(Arrays.asList(aVar));
		if (someVariableOccurrences.containsKey(set)) {
			Set<CollapsedVariablesPosset> occurrences = someVariableOccurrences.get(set);

			if (!occurrences.contains(aPosset)) {
				occurrences.add(aPosset);
			} else {
				LOG.error("The node called {} should not be already registered for var {}.", aPosset, aVar);
			}
		} else {

			Set<CollapsedVariablesPosset> newSet = new HashSet<CollapsedVariablesPosset>();
			newSet.add(aPosset);
			someVariableOccurrences.put(set, newSet);
		}
	}

	private String getNormalizedVar(String aVar) {
		String[] splitted = aVar.split("&");
		Set<String> uniqueVars = new HashSet<String>(Arrays.asList(splitted));
		String newVar = "";
		for (String var : uniqueVars) {
			newVar += var + "&";
		}

		return newVar.substring(0, newVar.length() - 2);
	}

	/**
	 * Different variables that are used on the same posset, can be merged. This
	 * happens in this method. It changes the incoming variableMapping.
	 * 
	 * @param variableMapping
	 * @throws IncompatibleVariableTargetsException
	 * @throws ChildAlreadyExistsException
	 */
	private void setOverlappingVariables(Map<Set<String>, Set<CollapsedVariablesPosset>> variableMapping)
			throws IncompatibleVariableTargetsException, ChildAlreadyExistsException {

		while (!variableMapping.isEmpty()) {

			// merge and add the variables.
			variableMapping = mergeVariables(variableMapping);

			for (Map.Entry<Set<String>, Set<CollapsedVariablesPosset>> entry : variableMapping.entrySet()) {

				// there is only one posset left, which will be
				// connected correctly in the tree. Note that in some cases (read: the templates
				// of powerpossets), the variables are already set, so we should keep those.
				for (CollapsedVariablesPosset pos2 : entry.getValue()) {
					pos2.setVar(joinVar(pos2.getVar(), entry.getKey()));
				}

			}

			// loop again, because only now all possets variables are set.
			LOG.info("****************************************************************************");
			logVariableMapping(variableMapping);

			// make sure we iterate this mapping in the correct order. The
			// order should be determined by whether a variable encompasses
			// another variable. If so, then the first variable should be
			// reconnected first.
			List<Set<String>> ordered = getTopologicallySortedKeys(variableMapping);

			Set<String> key = ordered.get(0);
			LOG.trace("Reconnecting possets belonging to variable {}.", key);

			// all possets in the set, should become one!
			Map<CollapsedVariablesPosset, CollapsedVariablesPosset> replacements = this
					.reconnectCombinedPosset(variableMapping.get(key));

			variableMapping.remove(key);

			// replace occurrences of replacements.keySet() in
			// variableMapping.values() with their counterpart.
			replaceWastedPossets(variableMapping, replacements);

		}
	}

	/**
	 * @param variableMapping
	 * @param key
	 * @param replacements
	 */
	private void replaceWastedPossets(Map<Set<String>, Set<CollapsedVariablesPosset>> variableMapping,
			Map<CollapsedVariablesPosset, CollapsedVariablesPosset> replacements) {
		int replaceCount = 0;
		for (CollapsedVariablesPosset from : replacements.keySet()) {
			CollapsedVariablesPosset to = replacements.get(from);

			for (Map.Entry<Set<String>, Set<CollapsedVariablesPosset>> varPossets : variableMapping.entrySet()) {

				// assumed equality based on == operator.
				if (varPossets.getValue().contains(from)) {

					to.setVar((to.getVar() != null ? (to.getVar() + "&") : "")
							+ (from.getVar() != null ? from.getVar() : ""));

					if (from instanceof DerivedPosset) {
						// copy restriction

						assert to instanceof DerivedPosset;

						copyRestriction((DerivedPosset) from, (DerivedPosset) to);
					}

					varPossets.getValue().remove(from);
					varPossets.getValue().add(to);
					replaceCount++;

					LOG.debug("Replaced {} with {}.", new Object[] { from, to, varPossets.getKey() });

				}
			}
		}
		LOG.info("Replaced {} possets.", replaceCount);
	}

	/**
	 * @param variableMapping
	 * @return
	 */
	private List<Set<String>> getTopologicallySortedKeys(
			Map<Set<String>, Set<CollapsedVariablesPosset>> variableMapping) {
		// create a directed acyclic graph from the variables and use a
		// topological sort to order them correctly.
		Iterator<Map.Entry<Set<String>, Set<CollapsedVariablesPosset>>> iter1 = variableMapping.entrySet().iterator();
		Iterator<Map.Entry<Set<String>, Set<CollapsedVariablesPosset>>> iter2 = variableMapping.entrySet().iterator();

		DirectedGraph<Set<String>> graph = new DirectedGraph<Set<String>>();

		for (Set<String> entry : variableMapping.keySet()) {
			graph.addNode(entry);
		}

		while (iter1.hasNext()) {
			Map.Entry<Set<String>, Set<CollapsedVariablesPosset>> entry1 = iter1.next();
			while (iter2.hasNext()) {
				Map.Entry<Set<String>, Set<CollapsedVariablesPosset>> entry2 = iter2.next();

				int i = this.compareEntries(entry1, entry2);
				if (i < 0) {
					// entry1 is smaller
					if (!graph.edgeExists(entry2.getKey(), entry1.getKey())) {
						graph.addEdge(entry2.getKey(), entry1.getKey());
						LOG.trace("Added edge from {} to {}.", entry2.getKey(), entry1.getKey());
					}
				} else if (i > 0) {
					// entry2 is smaller
					if (!graph.edgeExists(entry1.getKey(), entry2.getKey())) {
						graph.addEdge(entry1.getKey(), entry2.getKey());
						LOG.trace("Added edge from {} to {}.", entry1.getKey(), entry2.getKey());
					}
				} else {
					// undefined, no edge
				}

			}
			iter2 = variableMapping.entrySet().iterator();
		}

		List<Set<String>> ordered = TopologicalSort.sort(graph);
		Collections.reverse(ordered); // somehow it sorts it the other way
										// around.

		LOG.trace("Partially ordered variables: {}", ordered);
		return ordered;
	}

	/**
	 * Merge variables whose sets of possets contain overlap.
	 * 
	 * @param variableMapping
	 * @return
	 */
	private Map<Set<String>, Set<CollapsedVariablesPosset>> mergeVariables(
			Map<Set<String>, Set<CollapsedVariablesPosset>> variableMapping) {
		java.util.Iterator<Set<String>> firstIter = variableMapping.keySet().iterator();

		java.util.Iterator<Set<String>> secondIter = null;

		Set<String> firstVar, secondVar, newVar;
		Set<CollapsedVariablesPosset> intersectSet, unionSet;
		boolean found;

		boolean overlappingFound = true;
		Map<Set<String>, Set<CollapsedVariablesPosset>> newVariableMapping;
		while (overlappingFound) {
			overlappingFound = false;
			newVariableMapping = new HashMap<Set<String>, Set<CollapsedVariablesPosset>>();

			while (firstIter.hasNext()) {
				firstVar = firstIter.next();
				found = false;
				secondIter = variableMapping.keySet().iterator();
				while (secondIter.hasNext()) {
					secondVar = secondIter.next();

					if (!firstVar.equals(secondVar)) {

						newVar = new HashSet<String>(firstVar);
						newVar.addAll(secondVar);

						if (!(newVariableMapping.containsKey(newVar))) {
							intersectSet = new HashSet<CollapsedVariablesPosset>(variableMapping.get(firstVar));
							intersectSet.retainAll(variableMapping.get(secondVar));

							if (!intersectSet.isEmpty()) {
								found = true;
								// overlapping variable found
								overlappingFound = true;
								// create union
								unionSet = new HashSet<CollapsedVariablesPosset>(variableMapping.get(firstVar));
								unionSet.addAll(variableMapping.get(secondVar));

								newVariableMapping.put(newVar, unionSet);

							}
						} else {
							found = true;
						}
					}
				}
				if (!found) {
					newVariableMapping.put(firstVar, variableMapping.get(firstVar));
				}
			}

			variableMapping = newVariableMapping;
			firstIter = variableMapping.keySet().iterator();
		}
		return variableMapping;
	}

	/**
	 * 
	 * @param from To copy the restriction from.
	 * @param to   TO copy the restriction to. Note that any restrictions already
	 *             here will be kept. If they are the same variable, they will be
	 *             merged.
	 */
	private void copyRestriction(DerivedPosset from, DerivedPosset to) {

		List<String> toHeader = to.getRestrictionHeader();
		if (toHeader == null)
			toHeader = new ArrayList<String>();

		List<List<Integer>> toValues = to.getRestrictionValues();
		if (toValues == null)
			toValues = new ArrayList<List<Integer>>();

		List<String> fromHeader = from.getRestrictionHeader();
		if (fromHeader == null)
			fromHeader = new ArrayList<String>();

		List<List<Integer>> fromValues = from.getRestrictionValues();
		if (fromValues == null)
			fromValues = new ArrayList<List<Integer>>();

		for (int idx = 0; idx < fromHeader.size(); idx++) {
			String header = fromHeader.get(idx);
			if (idx >= 0) {
				// header already exists, so add it only to the values
				toValues.get(idx).addAll(fromValues.get(idx));
			} else {
				// header does not already exist, so add the header and the
				// values
				toHeader.add(header);
				toValues.add(fromValues.get(idx));
			}
		}

		to.setRestrictionHeader(toHeader.isEmpty() ? null : toHeader);
		to.setRestrictionValues(toValues.isEmpty() ? null : toValues);
	}

	/**
	 * 
	 * @param o1
	 * @param o2
	 * @return -1 if the first should be before the second, 0 if it is undefined and
	 *         1 if the second should become before the first.
	 */
	public int compareEntries(Entry<Set<String>, Set<CollapsedVariablesPosset>> o1,
			Entry<Set<String>, Set<CollapsedVariablesPosset>> o2) {

		boolean o1EncompassesO2 = encompasses(o1, o2);
		boolean o2EncompassesO1 = encompasses(o2, o1);
		if (o1EncompassesO2 && o2EncompassesO1) {
			// if o1 encompasses o2 and o1 overlaps o2 and o2 overlaps o1 then
			// an exception is thrown.
			LOG.error("Variable {} and {} encompass each other for a deathlock.", o1.getKey(), o2.getKey());
		} else if (o1EncompassesO2) {
			// if o1 overlaps o2 and o1 encompasses o2, then o1 is smaller than
			// o2.
			return -1;
		} else if (o2EncompassesO1) {
			// if o1 overlaps with o2 and o2 encompoasses o1, then o1 is bigger
			// than o2.
			return 1;
		}

		return 0;
	}

	/**
	 * @param o1
	 * @param o2
	 * @return whether o1 encompasses o2. This means, whether one of o1's possets
	 *         exists on one of the paths of o2 to the root of the posset tree.
	 */
	public boolean encompasses(Entry<Set<String>, Set<CollapsedVariablesPosset>> o1,
			Entry<Set<String>, Set<CollapsedVariablesPosset>> o2) {

		// loop the possets of o1 and check for each of them whether it
		// occurs on any of the path from any of the possets of o2. If there
		// exists
		// at least one of them, we know o1 encompasses o2 and return true.

		Set<CollapsedVariablesPosset> o1Possets = o1.getValue();
		Set<CollapsedVariablesPosset> o2Possets = o2.getValue();
		Iterator<CollapsedVariablesPosset> o1Iter = o1Possets.iterator();
		Iterator<CollapsedVariablesPosset> o2Iter = o2Possets.iterator();

		while (o1Iter.hasNext()) {
			CollapsedVariablesPosset pos1 = o1Iter.next();
			while (o2Iter.hasNext()) {
				CollapsedVariablesPosset pos2 = o2Iter.next();
				if (occursOnRootPath(pos1, pos2)) {
					return true;
				}
			}
			o2Iter = o2Possets.iterator();
		}

		return false;
	}

	/**
	 * 
	 * @param o1 The posset to check whether it occurs on the path.
	 * @param o2 The posset for which to check if on any of its paths to the root o1
	 *           occurs.
	 * @return Whether posset o1 occurs on one of the root paths of o2.
	 */
	public boolean occursOnRootPath(CollapsedVariablesPosset o1, CollapsedVariablesPosset o2) {

		Queue<CollapsedVariablesPosset> toCheck = new LinkedList<CollapsedVariablesPosset>(o2.getParents());

		while (!toCheck.isEmpty()) {
			CollapsedVariablesPosset check = toCheck.poll();

			if (o1.equals(check)) {
				return true;
			} else {
				toCheck.addAll(check.getParents());
			}
		}

		return false;
	}

	/**
	 * Merge new vars with existing vars and merge new vars with each other.
	 * 
	 * @param existingVars
	 * @param aVar
	 * @return
	 */
	private String joinVar(String existingVars, Set<String> aVar) {
		Set<String> newVar = new TreeSet<String>(aVar);
		// first add the existinVars
		if (existingVars != null) {
			String[] splitted = existingVars.split("&");
			newVar.addAll(Arrays.asList(splitted));
		}
		String joined = "";

		for (String s : newVar) {
			joined += "&" + s;
		}

		return joined.substring(1);
	}

	/**
	 * Returns the posset which is the combination of the given possets. It seeks
	 * the common ancenstor of all possets and stacks all other parts above each
	 * other. In practice intersecting their possy sets.
	 * 
	 * @param unionSet
	 * @return A mapping from waste possets and their replacement. Because possets
	 *         are combined, large pieces of the tree will become disconnected.
	 *         These tree-parts contain possets with variables that still reside in
	 *         the variables mapping and should be replaced by their counterpart
	 *         after reconnection.
	 * @throws IncompatibleVariableTargetsException
	 * @throws ChildAlreadyExistsException
	 */
	private Map<CollapsedVariablesPosset, CollapsedVariablesPosset> reconnectCombinedPosset(
			Set<CollapsedVariablesPosset> unionSet)
			throws IncompatibleVariableTargetsException, ChildAlreadyExistsException {

		Map<CollapsedVariablesPosset, CollapsedVariablesPosset> replacementMapping = new HashMap<CollapsedVariablesPosset, CollapsedVariablesPosset>();
		CollapsedVariablesPosset highestCommonAncestorCandidate = null;
		CollapsedVariablesPosset combinedPosset = null;
		CollapsedVariablesPosset p = null;
		DerivedPosset pDerived = null;
		PrimePosset pPrime = null;

		List<CollapsedVariablesPosset> unionList = new ArrayList<CollapsedVariablesPosset>(unionSet);

		// Does the problem of fluctuating possies disappear when I sort this
		// list before processing? No, it does not disappear, but debugging does
		// get easier.
		// We also want AnyPossets to be at the end of the list, because they should
		// never be candidates.
		Collections.sort(unionList, new Comparator<CollapsedVariablesPosset>() {

			public int compare(CollapsedVariablesPosset o1, CollapsedVariablesPosset o2) {

				if (o1 instanceof AnyPosset)
					return 1;
				if (o2 instanceof AnyPosset)
					return -1;

				return o1.getName().compareTo(o2.getName());
			}

		});

		List<Boolean> hasCandidate = new ArrayList<Boolean>(unionSet.size());

		boolean hasMore = true;
		boolean foundCandidate = false;

		// we use the first posset in the list to check if one of the possets in
		// the hierarchy is common to all others, if not, there is no candidate.
		highestCommonAncestorCandidate = unionList.get(0);

		LOG.trace("Parents of candidate: {}", highestCommonAncestorCandidate.getParents());

		while (!foundCandidate && hasMore) {

			foundCandidate = true;
			// we skip the first because that is our pivot.
			for (int i = 1; i < unionList.size() && foundCandidate; i++) {

				p = unionList.get(i);
				foundCandidate &= p.hasAncestor(highestCommonAncestorCandidate);
			}

			if (!foundCandidate && highestCommonAncestorCandidate instanceof DerivedPosset) {
				pDerived = (DerivedPosset) highestCommonAncestorCandidate;

				if (pDerived.getChildNames().size() == 1) {
					// we can go further down.
					highestCommonAncestorCandidate = pDerived.getChildren().iterator().next();
				} else {
					// this is the end
					hasMore = false;
					highestCommonAncestorCandidate = null;
				}

			} else if (!foundCandidate && highestCommonAncestorCandidate instanceof PrimePosset) {
				hasMore = false;
				highestCommonAncestorCandidate = null;
			} else if (!foundCandidate && highestCommonAncestorCandidate instanceof CVPowerPosset) {
				// this is the end
				hasMore = false;
				highestCommonAncestorCandidate = null;
			}
		}

		if (highestCommonAncestorCandidate != null) {
			// reuse the highestCommonAncestorCandidate to let all others refer
			// to it, instead of their own.

			// we construct the new posset from the first one!
			combinedPosset = unionList.get(0);

			for (int i = 1; i < unionList.size(); i++) {
				Posset p2 = null;
				p = unionList.get(i);
				p2 = p.getAboveAncestor(p, highestCommonAncestorCandidate);
				if (p2 != null) {
					if (p2 instanceof DerivedPosset) {
						DerivedPosset aboveAncestor = (DerivedPosset) p2;

						// add the tree part between the variable and p2
						// between combinedPosset and its parent (and all other
						// parents it already has from previous iterations).

						// <remark> if p and combinedPosset share a parent, it
						// only occurs in the resulting parent set once. This
						// might be a problem in the future. <answer> This is no
						// longer a problem, because children are stored in a
						// mapping from component name to Posset.

						// everything below the highest common ancestor will be
						// garbage collected, but not just yet, because we need
						// to retain the information about variables in this
						// tree-part.
						CollapsedVariablesPosset wastedPosset = p.getAncestor(highestCommonAncestorCandidate);
						assert combinedPosset.getAncestor(
								highestCommonAncestorCandidate) == highestCommonAncestorCandidate : "The ancestor of combinedPosset should be exactly the same object.";

						for (Posset p3 : combinedPosset.getParents()) {
							if (p3 instanceof DerivedPosset) {
								DerivedPosset p4 = (DerivedPosset) p3;
								Set<String> theNames = this.getNamesInParent(combinedPosset, p4);
								// automatically adds as parent
								for (String aName : theNames) {
									p4.setChild(aName, p);
								}
							} else if (p3 instanceof CVPowerPosset) {
								var p4 = (CVPowerPosset) p3;
								p4.setPowerOfPosset(p);
							}
						}

						combinedPosset.copyPrimeVars(aboveAncestor.getChildren().iterator().next());

						combinedPosset.getParents().clear();
						// automatically adds as parent
						aboveAncestor.setChild(aboveAncestor.getChildNames().iterator().next(), combinedPosset);

						combinedPosset = p;

						// all possets within tree-parts that get
						// disconnected, should be replaced by their
						// combinedPosset twin.

						replacementMapping.putAll(findReplacements(wastedPosset, highestCommonAncestorCandidate));

					} else {
						LOG.error("The result of the getAboveAncestor should always be a DerivedPosset and not a {}.",
								p2.getClass().getSimpleName());
					}
				} else {
					// we just reconnect to the first common ancestor.

					// <remark> we assume single parent, is this correct at this
					// stage? <answer> Yes, this is correct, since p is not yet
					// processed it only has a single parent.

					CollapsedVariablesPosset cvp = p.getParents().iterator().next();
					if (cvp instanceof DerivedPosset) {
						DerivedPosset parentP = (DerivedPosset) cvp;
						Set<String> theNames = this.getNamesInParent(p, parentP);

						highestCommonAncestorCandidate.copyPrimeVars(p);
						for (String aName : theNames) {
							parentP.setChild(aName, combinedPosset);
						}
					} else if (cvp instanceof CVPowerPosset) {
						CVPowerPosset powerP = (CVPowerPosset) cvp;
						highestCommonAncestorCandidate.copyPrimeVars(p);
						powerP.setPowerOfPosset(combinedPosset);
					} else if (cvp instanceof PickPosset) {
						LOG.debug("Found the pick operator, connecting possets correctly.");
						PickPosset pickPosset = (PickPosset) cvp;
//						highestCommonAncestorCandidate.copyPrimeVars(p);

						if (pickPosset.getPosset() == p) {
							pickPosset.setPosset(combinedPosset);
						} else if (pickPosset.getElemPosset() == p) {
							pickPosset.setElemPosset(combinedPosset);
						} else if (pickPosset.getRestPosset() == p) {
							pickPosset.setRestPosset(combinedPosset);
						}
					}

					if (!(cvp instanceof PickPosset)) {
						// all possets within tree-parts that get
						// disconnected, should be replaced by their
						// combinedPosset twin.

						// make sure the counterpart and wasted posset have the same
						// structure. Since findReplacements() expects this.
						CollapsedVariablesPosset counter = combinedPosset.getAncestor(p);

						replacementMapping.putAll(findReplacements(p, counter));
					}

				}
			}
		} else if (unionList.size() > 1)

		{
			// a variable that could not be merged. Throw incompatible variable
			// target exception.
			throw new IncompatibleVariableTargetsException("Could not collapse incompatible possets: " + unionList);
		}
		LOG.trace("Created combined posset: {}", combinedPosset);

		return replacementMapping;
	}

	private Map<CollapsedVariablesPosset, CollapsedVariablesPosset> findReplacements(
			CollapsedVariablesPosset wastedPosset, CollapsedVariablesPosset counterPart) {

		Map<CollapsedVariablesPosset, CollapsedVariablesPosset> replacements = new HashMap<CollapsedVariablesPosset, CollapsedVariablesPosset>();

		CollapsedVariablesPosset from = wastedPosset;
		CollapsedVariablesPosset to = counterPart;

		// breadth-first search through the two trees.
		Queue<CollapsedVariablesPosset> fromQueue = new LinkedList<CollapsedVariablesPosset>();
		Queue<CollapsedVariablesPosset> toQueue = new LinkedList<CollapsedVariablesPosset>();

		fromQueue.add(from);
		toQueue.add(to);

		CollapsedVariablesPosset posFrom;
		CollapsedVariablesPosset posTo;

		CollapsedVariablesPosset chFrom;
		CollapsedVariablesPosset chTo;

		while (!fromQueue.isEmpty()) {
			posFrom = fromQueue.poll();
			posTo = toQueue.poll();
			assert posFrom.getName().equals(posTo.getName());
			replacements.put(posFrom, posTo);
			if (posFrom instanceof DerivedPosset) {
				// derived, add children to queue.
				DerivedPosset derFrom = (DerivedPosset) posFrom;

				assert posTo instanceof DerivedPosset;
				DerivedPosset derTo = (DerivedPosset) posTo;

				// add corresponding children to the queue.
				for (String child : derFrom.getChildNames()) {
					chFrom = derFrom.getChildPosset(child);
					chTo = derTo.getChildPosset(child);

					assert chFrom != null;
					assert chTo != null;

					fromQueue.add(chFrom);
					toQueue.add(chTo);
				}
			} else {
				// prime do nothing
			}
		}

		return replacements;
	}

	/**
	 * find the name of p in its parent.
	 * 
	 * @param p
	 * @return
	 */
	private Set<String> getNamesInParent(CollapsedVariablesPosset p, DerivedPosset parent) {
		Set<String> names = new HashSet<String>();

		for (String p2 : parent.getChildNames()) {
			CollapsedVariablesPosset p3 = parent.getChildPosset(p2);
			if (p3 == p) {
				names.add(p2);
			}
		}

		return names;
	}
}
