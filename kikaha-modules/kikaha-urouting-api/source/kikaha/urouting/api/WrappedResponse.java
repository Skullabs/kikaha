package kikaha.urouting.api;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors( fluent=true )
@RequiredArgsConstructor
final public class WrappedResponse implements Response {

	final Response wrapped;

	Object entity;
	int statusCode;
	String encoding;
	String contentType;

	@Override
	public Iterable<Header> headers() {
		return wrapped.headers();
	}

	@Override
	public Object entity() {
		if ( entity != null )
			return entity;
		return wrapped.entity();
	}

	@Override
	public int statusCode() {
		if ( statusCode > 0 )
			return statusCode;
		return wrapped.statusCode();
	}

	@Override
	public String encoding() {
		if ( encoding != null )
			return encoding;
		return wrapped.encoding();
	}

	@Override
	public String contentType() {
		if ( contentType != null )
			return contentType;
		return wrapped.contentType();
	}
}