package io.skullabs.undertow.urouting.serializers;

import io.skullabs.undertow.urouting.api.Mimes;
import io.skullabs.undertow.urouting.api.RoutingException;
import io.skullabs.undertow.urouting.api.Serializer;

import java.io.IOException;
import java.io.Writer;

import trip.spi.Singleton;

@Singleton( name = Mimes.PLAIN_TEXT )
public class PlainTextSerializer implements Serializer {

	static final String NULL = "null";

	@Override
	public <T> void serialize( T object, Writer output ) throws RoutingException {
		try {
			String serialized = object != null ? object.toString() : NULL;
			output.write( serialized );
		} catch ( IOException cause ) {
			throw new RoutingException( cause );
		}
	}
}