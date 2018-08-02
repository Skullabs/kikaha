package kikaha.uworkers.sqs;

import java.io.IOException;
import java.lang.UnsupportedOperationException;
import java.util.function.BiConsumer;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import kikaha.uworkers.api.*;
import lombok.RequiredArgsConstructor;

/**
 * An {@link Exchange} implementation to store SQS messages.
 */
@RequiredArgsConstructor
public class SQSExchange implements Exchange {

	final ObjectMapper mapper;
	final AmazonSQS sqs;
	final String queueUrl;
	final Message message;

	@Override
	public Exchange acknowledge() {
		final DeleteMessageRequest request = new DeleteMessageRequest(queueUrl, message.getReceiptHandle());
		sqs.deleteMessage( request );
		return this;
	}

	@Override
	public <REQ> REQ requestAs( Class<REQ> requestClass ) throws IOException {
		final String body = message.getBody();
		return mapper.readValue( body, requestClass );
	}

	@Override
	public <REQ> REQ request() {
		throw new UnsupportedOperationException("request not available on SQS' Exchange!");
	}

	@Override
	public <RESP> RESP response() {
		throw new UnsupportedOperationException("create not available on SQS' Exchange!");
	}

	@Override
	public <RESP> RESP responseAs( Class<RESP> responseClass ) {
		throw new UnsupportedOperationException("responseAs not available on SQS' Exchange!");
	}

	@Override
	public <RESP> Exchange reply(RESP response) {
		throw new UnsupportedOperationException("reply not available on SQS' Exchange!");
	}

	@Override
	public Exchange reply(Throwable error) {
		throw new UnsupportedOperationException("reply not available on SQS' Exchange!");
	}

	@Override
	public Response then(BiConsumer<UndefinedObject, Throwable> listener) {
		throw new UnsupportedOperationException("onFinish not available on SQS' Exchange!");
	}
}
