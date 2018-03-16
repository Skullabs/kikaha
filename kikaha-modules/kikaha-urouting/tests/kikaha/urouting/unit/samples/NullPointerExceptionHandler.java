package kikaha.urouting.unit.samples;

import kikaha.urouting.api.ExceptionHandler;
import kikaha.urouting.api.Response;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;

@Singleton
@Typed( ExceptionHandler.class )
public class NullPointerExceptionHandler implements ExceptionHandler<NullPointerException> {

	@Override
	public Response handle( NullPointerException exception ) {
		final StringWriter writer = new StringWriter();
		exception.printStackTrace( new PrintWriter( writer ) );
		return Response.serverError( writer.toString() );
	}
}
