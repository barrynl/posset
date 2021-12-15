/**
 * 
 */
package syntax;

/**
 * Thrown when a power posset is defined with zero or more than 1 posset.
 * 
 * @author Barry
 * 
 */
public class OnlySingleChildAllowedException extends SyntaxException {

	public OnlySingleChildAllowedException(String aMessage) {
		super(aMessage);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
