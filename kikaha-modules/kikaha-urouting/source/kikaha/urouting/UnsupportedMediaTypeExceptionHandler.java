package kikaha.urouting;

import kikaha.urouting.api.ExceptionHandler;
import kikaha.urouting.api.Response;

import javax.inject.Singleton;

import static kikaha.urouting.api.Response.response;

/**
 *
 */
@Singleton
public class UnsupportedMediaTypeExceptionHandler
	implements ExceptionHandler<UnsupportedMediaTypeException> {

	@Override
	public Response handle(UnsupportedMediaTypeException exception) {
		return response( 415 ).entity( "Unsupported Media Type: " + exception.getMessage() );
	}
}
