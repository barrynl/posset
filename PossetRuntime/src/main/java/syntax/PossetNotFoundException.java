package syntax;

public class PossetNotFoundException extends SyntaxException {

	public PossetNotFoundException(String aMessage) {
		super(aMessage);
	}

	public PossetNotFoundException(String aMessage, Throwable t) {
		super(aMessage, t);
	}

	private static final long serialVersionUID = -615341323369588708L;

}
