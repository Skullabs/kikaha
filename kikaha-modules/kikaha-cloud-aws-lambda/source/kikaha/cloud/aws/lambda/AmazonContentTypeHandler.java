package kikaha.cloud.aws.lambda;

/**
 *
 */
public interface AmazonContentTypeHandler {

	String serialize( Object object ) throws AmazonRequestException;

	<T> T unserialize( String input, Class<T> target ) throws AmazonRequestException;

    String contentType();

    default boolean willBeBase64Encoded() {
        return false;
    }
}
