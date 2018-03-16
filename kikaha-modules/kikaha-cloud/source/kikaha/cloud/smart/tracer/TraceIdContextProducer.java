package kikaha.cloud.smart.tracer;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import kikaha.urouting.ContextProducer;
import kikaha.urouting.api.RoutingException;
import lombok.RequiredArgsConstructor;

/**
 * Created by ibratan on 16/06/2017.
 */
@RequiredArgsConstructor
public class TraceIdContextProducer implements ContextProducer<TraceId> {

    final HttpString TRACE_ID_HEADER;

    @Override
    public TraceId produce(HttpServerExchange exchange) throws RoutingException {
        final HeaderMap responseHeaders = exchange.getResponseHeaders();
        final String traceIdHeader = responseHeaders.getFirst(TRACE_ID_HEADER);
        return new TraceId( traceIdHeader );
    }
}