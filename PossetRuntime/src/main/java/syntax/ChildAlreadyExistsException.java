/**
 * 
 */
package syntax;

/**
 * Thrown when a posset is defined two or more times the same childname.
 * 
 * <pre>
 * main
 * (
 * 	'2 test;
 * 	'3 test;
 * )
 * </pre>
 * 
 * @author Barry
 * 
 */
public class ChildAlreadyExistsException extends SyntaxException {

	public ChildAlreadyExistsException(String aMessage) {
		super(aMessage);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
