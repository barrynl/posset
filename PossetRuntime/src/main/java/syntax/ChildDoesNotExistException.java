/**
 * 
 */
package syntax;

/**
 * Fired when a child is referenced with a name that does not exist for that
 * particular posset. For example:
 * 
 * <pre>
 * main
 * (
 * 	Test t1
 * 	(
 * 		firstTest[A];
 * 	)
 * 	'2 t2[A];
 * )
 * 
 * Test
 * (
 * 	'2 first;
 * )
 * 
 * </pre>
 * 
 * will throw this exception because posset Test does not have a child named
 * 'firstTest'.
 * 
 * @author Barry
 * 
 */
public class ChildDoesNotExistException extends SyntaxException {

	public ChildDoesNotExistException(String aMessage) {
		super(aMessage);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
