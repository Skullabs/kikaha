package kikaha.uworkers.core;

import java.util.function.BiConsumer;
import kikaha.uworkers.api.Exchange;
import lombok.RequiredArgsConstructor;

/**
 * Wraps an exchange to send a new value as reply to a former sender.
 */
@RequiredArgsConstructor(staticName = "wrap")
public class WrappedExchange implements Exchange {

	final Object request;
	final Exchange listener;

	@Override
	public <REQ> REQ requestAs( Class<REQ> requestType ) {
		return (REQ)request;
	}

	@Override
	public <REQ> REQ request() {
		return (REQ)request;
	}

	@Override
	public <RESP> Exchange reply( RESP response ) {
		listener.reply( response );
		return this;
	}

	@Override
	public Exchange reply(Throwable error) {
		listener.reply(error);
		return this;
	}

	@Override
	public Exchange then(BiConsumer<UndefinedObject, Throwable> listener) {
		this.listener.then( listener );
		return this;
	}

	@Override
	public <RESP> RESP response() {
		throw new UnsupportedOperationException("create not available here!");
	}

	@Override
	public <RESP> RESP responseAs(Class<RESP> targetClass) {
		throw new UnsupportedOperationException("responseAs not available here!");
	}
}
