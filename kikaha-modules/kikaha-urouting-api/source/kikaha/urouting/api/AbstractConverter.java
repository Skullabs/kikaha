package kikaha.urouting.api;

import kikaha.urouting.api.converter.ConversionException;

import java.lang.reflect.ParameterizedType;

/**
 * Abstract class to provide a conversion from String to {@code T}. It is hugely
 * used during conversion of Header/Cookie/Path/Query Parameters.
 *
 * @param <T>
 */
public abstract class AbstractConverter<T> {

	public abstract T convert( String value ) throws ConversionException;

	@SuppressWarnings( "unchecked" )
	public Class<T> getGenericClass() {
		return (Class<T>)( (ParameterizedType)getClass().getGenericSuperclass() )
				.getActualTypeArguments()[0];
	}
}