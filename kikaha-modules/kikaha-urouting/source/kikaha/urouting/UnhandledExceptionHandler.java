package kikaha.urouting;

import kikaha.urouting.api.ExceptionHandler;
import kikaha.urouting.api.Response;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class UnhandledExceptionHandler implements ExceptionHandler<Throwable> {

	@Override
	public Response handle( final Throwable exception ) {
		log.error("Unhandled exception", exception);
		String msg = NullPointerException.class.equals(exception.getClass())
				? "NullPointerException" : exception.getMessage();
		return Response.serverError( msg );
	}
}
