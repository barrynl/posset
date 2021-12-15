/**
 * 
 */
package nl.barry.posset.runtime;

import java.util.Map;

import syntax.SyntaxException;

/**
 * TO generate a runtime posset from a Abstract Syntax Tree. Make a SPI of this.
 * To choose the compiler to use.
 * 
 * @author Barry
 * 
 */
public interface PossetFactory {

	public abstract Posset getRuntimePosset(
			Map<String, nl.barry.posset.ast.Posset> nameMapping, String name)
			throws SyntaxException;

}
