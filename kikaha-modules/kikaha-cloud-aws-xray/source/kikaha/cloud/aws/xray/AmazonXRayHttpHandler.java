package kikaha.cloud.aws.xray;

import java.util.*;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Segment;
import io.undertow.server.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
public class AmazonXRayHttpHandler implements HttpHandler {

	final AWSXRayRecorder recorder;
	final SegmentFactory segmentFactory;
	final HttpHandler nextHandler;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		final Segment segment = segmentFactory.createSegment(exchange);
		exchange.addResponseCommitListener( new SegmentSender( recorder, segment ) );
		try {
			nextHandler.handleRequest( exchange );
		} catch (Exception cause) {
			if(null != segment)
				segment.addException(cause);
			throw cause;
		}
	}
}

@Slf4j
@RequiredArgsConstructor
class SegmentSender implements ResponseCommitListener {

	final AWSXRayRecorder recorder;
	final Segment segment;

	@Override
	public void beforeCommit(HttpServerExchange exchange) {
		try {
			attachResponseDataToSegment( exchange );
			if (segment.end())
				recorder.sendSegment(segment);
		} catch ( final Throwable cause ) {
			log.error( "Could not send segment " + segment, cause );
		}
	}

	void attachResponseDataToSegment( HttpServerExchange exchange ){
		final Map<String, Object> attributes = new HashMap<>();
		attributes.put( "status", exchange.getStatusCode() );
		final long contentLength = exchange.getResponseContentLength();
		if ( contentLength > -1 )
			attributes.put( "content_length", contentLength );

		segment.putHttp( "create", attributes );
	}
}