package kikaha.cloud.aws.lambda;

/**
 *
 */
public interface AmazonHttpFilter extends Comparable<AmazonHttpFilter> {

    default void doFilter(AmazonHttpRequest request) throws Exception {
        doFilter( request, null );
    }

    void doFilter(AmazonHttpRequest request, AmazonHttpFilter next) throws Exception;

    default void stopExecution( AmazonHttpResponse response ) {}

    @Override
    default int compareTo(AmazonHttpFilter filter){
        return 0;
    }
}
