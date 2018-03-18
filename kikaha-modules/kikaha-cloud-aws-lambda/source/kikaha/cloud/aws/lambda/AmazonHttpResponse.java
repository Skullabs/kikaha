package kikaha.cloud.aws.lambda;

import java.util.*;
import kikaha.urouting.api.*;
import lombok.*;
import lombok.experimental.Accessors;

/**
 *
 */
@Getter @Setter @Accessors(chain = true)
@RequiredArgsConstructor
public class AmazonHttpResponse {

	private static final Map<String, String> CONTENT_TYPE_JSON = Collections.singletonMap( Headers.CONTENT_TYPE, Mimes.JSON );

	int statusCode;
	Map<String, String> headers;
	String body;

	@Setter( AccessLevel.PACKAGE )
	transient Object unserializedBody;

	boolean isBase64Encoded = false;

    public void serializeWith(AmazonContentTypeHandlers serializers ) {
        val contentType = getContentType();
        if ( contentType != null && unserializedBody != null ) {
            val serializer = serializers.get( contentType );
            this.setBody(serializer.serialize( unserializedBody ));
            this.setBase64Encoded( serializer.willBeBase64Encoded() );
            this.setUnserializedBody( null );
        }
    }

    public AmazonHttpResponse setBody(String body ) {
        this.body = body;
        this.unserializedBody = null;
        return this;
    }

    public AmazonHttpResponse setBody(Object body ) {
        this.unserializedBody = body;
        this.body = null;
        return this;
    }

    public String getContentType() {
        return headers.get( Headers.CONTENT_TYPE );
    }

	public static AmazonHttpResponse with(Response response ) {
		val headers = new HashMap<String, String>();
		for ( val header : response.headers() )
            for (val value : header.values())
                headers.put(header.name(), value);

		return new AmazonHttpResponse()
            .setStatusCode( response.statusCode() )
            .setHeaders( headers )
            .setBody( response.entity() );
	}

	public static AmazonHttpResponse with(Object body ) {
		return new AmazonHttpResponse()
            .setStatusCode(200)
            .setHeaders( CONTENT_TYPE_JSON )
            .setBody( body );
	}

	public static AmazonHttpResponse noContent() {
		return AmazonHttpResponse.create( 204 );
	}

	public static AmazonHttpResponse notFound() {
		return AmazonHttpResponse.create( 404 );
	}

    public static AmazonHttpResponse notAuthenticated() {
		return AmazonHttpResponse.create( 401 );
    }

    public static AmazonHttpResponse create(int statusCode ) {
        return create( statusCode, Collections.emptyMap() );
    }

    public static AmazonHttpResponse create(int statusCode, Map<String, String> headers ) {
        return new AmazonHttpResponse()
            .setStatusCode( statusCode )
            .setHeaders( headers );
    }
}
