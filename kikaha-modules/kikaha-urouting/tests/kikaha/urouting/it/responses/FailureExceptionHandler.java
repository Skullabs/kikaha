package kikaha.urouting.it.responses;

import javax.inject.Singleton;
import kikaha.urouting.api.*;

/**
 *
 */
@Singleton
public class FailureExceptionHandler implements ExceptionHandler<FailureException> {

	@Override
	public Response handle( FailureException exception ) {
		return Response.serverError( "Failed!" );
	}
}
