package kikaha.protobuf;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.google.protobuf.Message;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import kikaha.core.modules.http.ContentType;
import kikaha.urouting.serializers.Serializer;

/**
 * @author: miere.teixeira
 */
@Singleton
@ContentType( ProtobufSerializer.MIME )
public class ProtobufSerializer implements Serializer {

	public static final String MIME = "application/octet-stream";

	@Override
	public <T> void serialize( final T object, final HttpServerExchange httpServerExchange, final String contentType ) throws IOException {
		final ByteBuffer bytes = ByteBuffer.wrap( ((Message) object ).toByteArray() );
		httpServerExchange.getResponseHeaders().add( Headers.CONTENT_TYPE, contentType );
		httpServerExchange.getResponseSender().send( bytes );
	}
}
