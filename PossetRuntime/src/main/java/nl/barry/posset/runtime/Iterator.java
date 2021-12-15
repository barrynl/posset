package nl.barry.posset.runtime;

public interface Iterator<T extends Element> extends java.util.Iterator<T> {

	void reset();

	T current();

}
