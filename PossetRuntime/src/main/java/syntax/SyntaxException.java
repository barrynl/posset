/**
 * 
 */
package syntax;

/**
 * @author Barry
 *
 */
public class SyntaxException extends Exception {

	public SyntaxException(String aMessage) {
		super(aMessage);
	}

	public SyntaxException(String aMessage, Throwable t) {
		super(aMessage, t);
	}
}
