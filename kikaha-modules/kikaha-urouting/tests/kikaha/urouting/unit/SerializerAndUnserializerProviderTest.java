package kikaha.urouting.unit;

import kikaha.core.test.KikahaRunner;
import kikaha.urouting.*;
import kikaha.urouting.api.Mimes;
import kikaha.urouting.serializers.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(KikahaRunner.class)
public class SerializerAndUnserializerProviderTest {

	@Inject
    SerializerAndUnserializerProvider provider;

	@Test
	public void ensureThatCanRetrieveAValidSerializerForTEXTPLAIN() throws IOException {
		final Serializer serializer = provider.getSerializerFor(Mimes.PLAIN_TEXT);
		assertTrue( PlainTextSerializer.class.isInstance( serializer ) );
	}

	@Test
	public void ensureThatCanRetrieveAValidUnserializerForTEXTPLAIN() throws IOException {
		final Unserializer unserializer = provider.getUnserializerFor(Mimes.PLAIN_TEXT);
		assertTrue( PlainTextUnserializer.class.isInstance( unserializer ) );
	}

	@Test( expected = UnsupportedMediaTypeException.class)
	public void ensureThatFailsToRetrieveAnInvalidSerializer() throws IOException {
		provider.getSerializerFor(Mimes.JSON);
	}

	@Test( expected = UnsupportedMediaTypeException.class)
	public void ensureThatFailsToRetrieveAnInvalidUnserializer() throws IOException {
		provider.getUnserializerFor(Mimes.JSON);
	}
}
