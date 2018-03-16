package kikaha.urouting.api;

import lombok.NonNull;

/**
 *
 */
public interface MutableResponse extends Response {

    MutableResponse statusCode( int status );

	/**
	 * Define the entity that will be serialized.
	 *
	 * @param entity
	 * @return
	 */
	MutableResponse entity( Object entity );

	/**
	 * Override all headers.
	 *
	 * @param headers
	 * @return
	 */
	MutableResponse headers( Iterable<Header> headers );

	/**
	 * Define the content encoding.
	 *
	 * @param encoding
	 * @return
	 */
	MutableResponse encoding( String encoding );

	/**
	 * Define the Content-Type defined with this {@link Response}.
	 *
	 * @param value
	 * @return
	 */
	default MutableResponse contentType( String value ) {
		return header( Headers.CONTENT_TYPE, value );
	}

	/**
	 * Defines a new header value.
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	MutableResponse header(final String name, @NonNull final String value );
}
