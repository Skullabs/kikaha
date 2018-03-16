package kikaha.urouting.api;

public class UnhandledException extends RuntimeException {

	private static final long serialVersionUID = -6945815136822946612L;

	public UnhandledException( Throwable throwable ) {
		super( throwable );
	}
}