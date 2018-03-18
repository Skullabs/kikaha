package kikaha.cloud.aws.lambda;

/**
 *
 */
public interface AmazonContextProducer<T> {

	T produce( AmazonHttpRequest request );
}
