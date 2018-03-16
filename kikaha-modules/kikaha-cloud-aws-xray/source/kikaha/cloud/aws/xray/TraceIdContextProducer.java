package kikaha.cloud.aws.xray;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import kikaha.cloud.smart.tracer.TraceId;
import kikaha.urouting.ContextProducer;
import kikaha.urouting.api.RoutingException;

import javax.inject.Singleton;

import static kikaha.cloud.aws.xray.SegmentFactory.X_AMZN_TRACE_ID;

/**
 * Created by miere.teixeira on 16/06/2017.
 */
@Singleton
public class TraceIdContextProducer implements ContextProducer<TraceId> {

    @Override
    public TraceId produce(HttpServerExchange exchange) throws RoutingException {
        final HeaderValues strings = exchange.getResponseHeaders().get(X_AMZN_TRACE_ID);
        return new TraceId( strings.getFirst() );
    }
}