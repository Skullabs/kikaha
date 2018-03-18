package kikaha.cloud.aws.lambda;

import kikaha.commons.url.URLMatcher;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Slf4j
@Singleton
public class AmazonRequestMatcher {

    @Inject @Typed( AmazonHttpHandler.class )
    Iterable<AmazonHttpHandler> amazonHttpHandlers;

    Map<String, List<Entry>> entriesMatcher;

    @PostConstruct
    public void loadHandlers() {
        entriesMatcher = new HashMap<>();

        log.info( "Registering AWS Lambda routes..." );
        for ( val handler : amazonHttpHandlers ) {
            val webResource = handler.getClass().getAnnotation( WebResource.class );
            val entries = entriesMatcher.computeIfAbsent( webResource.method(), k -> new ArrayList<>() );
            val entry = new Entry( webResource.path().replaceFirst( "/$", "" ), webResource.method(), handler );
            log.info( "  > " + entry );
            entries.add( entry );
        }
    }

    AmazonDefaultHttpHandler retrieveHttpHandler( AmazonHttpRequest request ) {
        val path = request.getPath().replaceFirst( "/$", "" );
        log.debug( "Retrieving HTTP Handler for request: " + request.httpMethod + " " + path );
        val list = entriesMatcher.getOrDefault( request.getHttpMethod(), Collections.emptyList() );
        for ( val entry : list )
            if ( entry.getMatcher().matches( path, request.getPathParameters() ) )
                return entry.getHandler();
        return null;
    }

    @Value
    private class Entry {

        Entry( final String url, final String method, final AmazonHttpHandler handler ) {
            this.asString = method + " " + url;
            this.handler = new AmazonDefaultHttpHandler( handler );
            this.matcher = URLMatcher.compile( url, true );
        }

        final String asString;
        final URLMatcher matcher;
        final AmazonDefaultHttpHandler handler;

        @Override
        public String toString(){
            return asString;
        }
    }

    @RequiredArgsConstructor
    static class AmazonDefaultHttpHandler implements AmazonHttpFilter {

        final AmazonHttpHandler amazonHttpHandler;

        @Override
        public void doFilter(AmazonHttpRequest request, AmazonHttpFilter next) throws Exception {
            log.debug( "Executing request..." );
            next.stopExecution( amazonHttpHandler.handle(request) );
        }
    }
}
