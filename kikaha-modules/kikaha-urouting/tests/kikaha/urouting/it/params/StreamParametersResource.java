package kikaha.urouting.it.params;

import kikaha.urouting.api.POST;
import kikaha.urouting.api.PUT;
import kikaha.urouting.api.Path;
import lombok.SneakyThrows;
import lombok.val;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author: ronei.gebert
 */
@Path( "it/parameters/is" )
@Singleton
public class StreamParametersResource {

	@POST
	public long pathParameterEchoWithPOSTMethod(InputStream is) {
		final byte[] bytes = read(is);
		final String number = "9".concat( new String(bytes) );
		return Long.valueOf( number );
	}

	@PUT
	public long pathParameterEchoWithPUTMethod(InputStream is) {
		return pathParameterEchoWithPOSTMethod( is );
	}

	@SneakyThrows
	public byte[] read(InputStream is) {
		val byteArrayStream = (ByteArrayInputStream) is;
		byte[] array = new byte[byteArrayStream.available()];
		byteArrayStream.read(array);
		return array;
	}

}
