package kikaha.cloud.aws.lambda;

/**
 *
 */
public interface AmazonHttpHandler {

	AmazonHttpResponse handle( AmazonHttpRequest request ) throws Exception;
}
