package kikaha.cloud.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import kikaha.core.cdi.DefaultCDI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Inject;

import static kikaha.cloud.aws.lambda.AmazonRequestException.handledException;

/**
 *
 */
@Slf4j @Getter @AllArgsConstructor
public class AmazonHttpApplication implements RequestHandler<AmazonHttpRequest, AmazonHttpResponse> {

	@Inject AmazonRequestMatcher requestMatcher;
	@Inject AmazonHttpFilters httpFilters;
	@Inject AmazonContentTypeHandlers serializer;

	public AmazonHttpApplication() {
        try {
            log.debug( "Initializing Lambda Application" );
            val cdi = DefaultCDI.newInstance();
            log.debug( "Injecting dependencies..." );
            cdi.injectOn( this );
            log.debug( "Injection process done!" );
            log.debug( "Handlers loaded and ready!" );
        } catch ( Throwable cause ) {
            throw handledException( cause );
        }
	}

	@Override
	public AmazonHttpResponse handleRequest( AmazonHttpRequest request, Context context ) {
		val httpHandler = requestMatcher.retrieveHttpHandler( request );
		if ( httpHandler == null )
			return AmazonHttpResponse.notFound();
        val chain = httpFilters.wrap( httpHandler );
        chain.doFilter( request );
        chain.response.serializeWith( serializer );
        return chain.response;
	}
}

