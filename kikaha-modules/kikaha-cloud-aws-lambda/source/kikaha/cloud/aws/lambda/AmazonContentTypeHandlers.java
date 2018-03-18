package kikaha.cloud.aws.lambda;

import lombok.*;

import java.util.*;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Typed;
import javax.inject.*;

@Singleton
public class AmazonContentTypeHandlers {

    @Inject @Typed( AmazonContentTypeHandler.class )
    List<AmazonContentTypeHandler> rawContentTypeHandlers;

    Map<String, AmazonContentTypeHandler> contentTypeHandlers;

    @PostConstruct
    public void loadContentTypeHandlers(){
        val handlers = new HashMap<String, AmazonContentTypeHandler>();
        for ( val handler : rawContentTypeHandlers )
            handlers.put( handler.contentType(), handler );
        contentTypeHandlers = handlers;
    }

    public AmazonContentTypeHandler get( String contentType ) {
        return contentTypeHandlers.computeIfAbsent( contentType, ct -> {
            throw new AmazonRequestException( "Unsupported request type:" + ct );
        });
    }
}
