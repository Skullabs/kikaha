package kikaha.urouting.serializers.jackson;

import kikaha.urouting.api.ExceptionHandler;
import kikaha.urouting.api.Response;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;

@Singleton
@Typed(ExceptionHandler.class)
public class JsonParseExceptionHandler
	implements ExceptionHandler<JsonProcessingException> {

	@Override
	public Response handle(final JsonProcessingException cause) {
		return Response
            .ok("BAD REQUEST: " + cause.getMessage())
            .statusCode(400);
	}
}
