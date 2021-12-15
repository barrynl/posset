/**
 * 
 */
package nl.barry.posset.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Barry
 *
 */
public abstract class CollapsedVariablesIterator<T extends Element> implements Iterator<T> {

	private CollapsedVariablesPosset generatedByPosset;

	public CollapsedVariablesIterator(CollapsedVariablesPosset p) {
		this.generatedByPosset = p;
	}

	/**
	 * Checks whether there is a next element that conforms to the given variable
	 * mapping (if the variable exists in this iterator or any of its sub
	 * iterators).
	 * 
	 * @param varMap
	 */
	@Override
	public boolean hasNext() {
		return this.hasNext(new HashMap<String, Integer>(), null);
	}

	public abstract boolean hasNext(Map<String, Integer> restriction, Parent parentIterator);

	@Override
	public T current() {
		return this.current(new HashMap<String, Integer>());
	}

	public abstract T current(Map<String, Integer> restriction);

	@Override
	public T next() {
		return this.next(new HashMap<String, Integer>(), null);
	}

	public abstract T next(Map<String, Integer> restriction, Parent parentIterator);

	@Override
	public void reset() {
		this.reset(null);
	}

	public abstract void reset(Parent parentIterator);

	public Posset getPosset() {
		return this.generatedByPosset;
	}

}
