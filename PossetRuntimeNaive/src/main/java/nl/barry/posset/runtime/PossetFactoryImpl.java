/**
 * 
 */
package nl.barry.posset.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.barry.posset.ast.Node;

/**
 * @author Barry
 * 
 */
public class PossetFactoryImpl implements PossetFactory {

	public Posset getRuntimePosset(
			Map<String, nl.barry.posset.ast.Posset> nameMapping, String name) {

		nl.barry.posset.ast.Posset astPosset = nameMapping.get(name);
		Posset rtPosset;
		if (astPosset != null) {

			Set<Node> nodes = astPosset.getChildNodes();
			DerivedPosset dPosset = new DerivedPosset(name);
			String nodeName;
			String nodeType;
			Posset rtChild;
			for (Node n : nodes) {
				nodeName = n.getName();
				nodeType = astPosset.getChildType(n);
				rtChild = this.getRuntimePosset(nameMapping, nodeType);
				dPosset.addPosset(nodeName, rtChild);
				Map<PossetPath, String> vars = findVariables(n);
				
				for(Map.Entry<PossetPath, String> entry: vars.entrySet())
				{
					dPosset.addVariable(entry.getKey(), entry.getValue());
				}
			}
			rtPosset = dPosset;
		} else {
			rtPosset = new PrimePosset(Integer.parseInt(name.substring(1)));
		}

		return rtPosset;
	}

	private Map<PossetPath, String> findVariables(Node n) {
		Map<PossetPath, String> vars = new HashMap<PossetPath, String>();
		if (n.getVar() != null)
		{
			PossetPath path = new PossetPath();
			path.add(n.getName());
			vars.put(path, n.getVar());
		}
		else
		{
			//if there is no variable, we do not need to extract one.
		}
		
		for(Node child: n.getChildren())
		{
			Map<PossetPath, String> childVars = findVariables(child);
			
			for(PossetPath path: childVars.keySet())
			{
				path.add(n.getName());
			}
		}
		
		return vars;
	}
}
