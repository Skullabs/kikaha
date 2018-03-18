package kikaha.cloud.aws.lambda;

import kikaha.core.cdi.CDI;
import kikaha.urouting.api.Mimes;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static kikaha.cloud.aws.lambda.AmazonRequestException.convertToString;

@Singleton @Slf4j
public class AmazonHttpFilters {

    @Getter( lazy = true )
    private final AmazonHttpFilter[] filters = loadFilters();

    @Inject CDI cdi;

    AmazonHttpFilter[] loadFilters(){
        val foundFilters = (List<AmazonHttpFilter>)cdi.loadAll( AmazonHttpFilter.class );
        Collections.sort( foundFilters );
        val filters = new AmazonHttpFilter[ foundFilters.size() ];
        foundFilters.toArray( filters );
        return filters;
    }

    AmazonHttpFilterChain wrap( AmazonHttpFilter filter ){
        val foundFilters = appendToChain( getFilters(), filter );
        return new AmazonHttpFilterChain( foundFilters );
    }

    AmazonHttpFilter[] appendToChain( AmazonHttpFilter[] current, AmazonHttpFilter filter ) {
        val filters = new AmazonHttpFilter[ current.length + 1 ];
        System.arraycopy( current, 0, filters, 0, current.length );
        filters[ current.length ] = filter;
        return filters;
    }

    @RequiredArgsConstructor
    class AmazonHttpFilterChain implements AmazonHttpFilter {

        @NonNull AmazonHttpFilter[] foundFilters;

        int cursor = 0;
        AmazonHttpResponse response = AmazonHttpResponse.noContent();

        @Override
        public void doFilter(AmazonHttpRequest request, AmazonHttpFilter next) {
            if ( next != null && next != this )
                foundFilters = appendToChain( foundFilters, next );
            doFilter( request );
        }

        @Override
        public void doFilter(AmazonHttpRequest request) {
            try {
                val filter = foundFilters[cursor++];
                filter.doFilter(request, this);
            } catch ( ArrayIndexOutOfBoundsException cause ) {
                stopExecution(
                    AmazonHttpResponse.create( 500 )
                        .setHeaders( Collections.singletonMap( "Content-Type", Mimes.PLAIN_TEXT ) )
                        .setBody( "Bad HTTP Handlers" )
                );
            } catch ( Throwable cause ) {
                stopExecution(
                        AmazonHttpResponse.create( 500 )
                                .setHeaders( Collections.singletonMap( "Content-Type", Mimes.PLAIN_TEXT ) )
                                .setBody( convertToString( cause ) )
                );
            }
        }

        @Override
        public void stopExecution( @NonNull AmazonHttpResponse response) {
            this.response = response;
        }
    }

    private static class InternalServerErrorFilter implements AmazonHttpFilter {

        @Override
        public void doFilter(AmazonHttpRequest request, AmazonHttpFilter next) throws Exception {
            next.stopExecution(
                AmazonHttpResponse.create( 500 ).setStatusCode( 500 )
                    .setBody( "Bad Filter" )
            );
        }
    }
}