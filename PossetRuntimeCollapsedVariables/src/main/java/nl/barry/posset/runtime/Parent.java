package nl.barry.posset.runtime;

/**
 * Holder object for parent together with the sub iter idx. Because only the parent is not enough to keep track.
 * @author Barry
 *
 */
public class Parent {

	private CollapsedVariablesIterator parent;
	private int index;
	
	public Parent(CollapsedVariablesIterator aParent, int anIndex)
	{
		this.parent = aParent;
		this.index = anIndex;
	}
	
	
	public CollapsedVariablesIterator getParent() {
		return parent;
	}
	public int getAnIndex() {
		return index;
	}


	@Override
	public String toString() {
		return "Parent [parent=" + parent + ", index=" + index + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Parent))
			return false;
		Parent other = (Parent) obj;
		if (index != other.index)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}
}
