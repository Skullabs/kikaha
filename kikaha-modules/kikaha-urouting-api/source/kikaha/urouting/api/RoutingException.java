package kikaha.urouting.api;

import java.io.IOException;

public class RoutingException extends IOException {

	private static final long serialVersionUID = -3572128955736929846L;

	public RoutingException( final String message, final Throwable cause ) {
		super( message, cause );
	}

	public RoutingException( final String message ) {
		super( message );
	}

	public RoutingException( final Throwable cause ) {
		super( cause );
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return super.fillInStackTrace();
	}
}
