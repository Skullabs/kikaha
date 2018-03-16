package kikaha.uworkers.sqs;

import java.util.function.BiConsumer;
import kikaha.uworkers.api.Response;

/**
 *
 */
public class SQSResponse implements Response {

	@Override
	public <RESP> RESP response() {
		throw new UnsupportedOperationException("create not implemented yet!");
	}

	@Override
	public <RESP> RESP responseAs(Class<RESP> targetClass) {
		throw new UnsupportedOperationException("responseAs not implemented yet!");
	}

	@Override
	public Response then(BiConsumer<UndefinedObject, Throwable> listener) {
		throw new UnsupportedOperationException("onFinish not implemented yet!");
	}
}
