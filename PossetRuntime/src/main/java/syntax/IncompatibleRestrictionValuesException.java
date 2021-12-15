/**
 * 
 */
package syntax;

/**
 * Thrown when the restriction values are incompatible with the restriction
 * header. For example:
 * 
 * <pre>
 * main
 * (
 * 	InitialTape t;
 * )
 * 
 * InitialTape
 * (
 * 	Tape t (
 * 		c2 (
 * 			d1(d1[A];d2[B];)
 * 			d2(d1[C];d2[D];)
 * 			d3(d1[E];d2[F];)
 * 		)
 * 	)
 * )
 * {
 * 	(A,B,C)
 * 	(#1,#1,#1,#1,#1,#1)
 * }
 * 
 * </pre>
 * 
 * The number of restriction headers is smaller than the number of restriction
 * values.
 * 
 * @author Barry
 * 
 */
public class IncompatibleRestrictionValuesException extends SyntaxException {

	public IncompatibleRestrictionValuesException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
