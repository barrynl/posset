/**
 * 
 */
package syntax;

/**
 * See syntax-errors/variable-deathlock.pos for code that throws this exception.
 * @author Barry
 *
 */
public class VariableDeathlockException extends SyntaxException {

	public VariableDeathlockException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}
