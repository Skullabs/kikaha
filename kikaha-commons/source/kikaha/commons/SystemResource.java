package kikaha.commons;

import java.io.*;

import lombok.NonNull;

/**
 *
 */
public interface SystemResource {

	String OS_NAME = System.getProperty("os.name").toLowerCase();
    boolean FILE_NAMES_ARE_CASE_SENSITIVE = !OS_NAME.contains("win");

	static String readFileAsString( String fileName, String encoding) {
		try ( InputStream file = openFile( fileName ) ) {
			if ( file == null )
				throw new IllegalStateException( "File not found: " + fileName );
			return readFileAsString( file, encoding );
		} catch ( IOException cause ) {
			throw new IllegalStateException( cause );
		}
	}

	static String readFileAsString(InputStream inputStream, String encoding) {
		try {
			return readFile(inputStream).toString(encoding);
		} catch ( UnsupportedEncodingException cause ) {
			throw new IllegalStateException( cause );
		}
	}

	static byte[] readFileAsBytes(InputStream inputStream) {
		return readFile(inputStream).toByteArray();
	}

	static ByteArrayOutputStream readFile( @NonNull final InputStream inputStream ) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			int length;
			while ( ( length = inputStream.read( buffer ) ) != -1 )
				baos.write( buffer, 0, length );
			return baos;
		} catch ( IOException cause ) {
			throw new IllegalStateException( cause );
		}
	}

	static InputStream openFile( String fileName ){
		InputStream stream = open(new File(fileName));
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if ( stream == null )
			stream = loader.getResourceAsStream( fileName );
		if ( stream == null ) {
			if ( fileName.charAt( 0 ) == '/' )
				fileName = fileName.substring(1);
			stream = loader.getResourceAsStream( fileName );
		}
		return stream;
	}

	static InputStream open( File file ) {
		try {
			if ( !file.exists() )
				file = new File(".", file.getPath());
			if ( file.exists() )
				return new FileInputStream( file );
			return null;
		} catch ( IOException cause ) {
			throw new IllegalStateException( cause );
		}
	}
}
