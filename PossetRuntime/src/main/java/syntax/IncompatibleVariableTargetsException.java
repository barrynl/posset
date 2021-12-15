/**
 * 
 */
package syntax;

/**
 * See syntax-errors/incompatible-variable-targets.pos for an explanation when
 * this error is thrown.
 * 
 * @author Barry
 * 
 */
public class IncompatibleVariableTargetsException extends SyntaxException {
	
	public IncompatibleVariableTargetsException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
