package kikaha.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.*;

import kikaha.commons.SystemResource;
import org.junit.Test;

/**
 * Unit tests for SystemResource.
 */
public class SystemResourceTest {

	final static String FILE_CONTENT = "server:\n" +
			"  https:\n" +
			"    keystore: \"server.keystore\"\n" +
			"    truststore: \"server.truststore\"\n" +
			"    cert-security-provider: \"TLS\"\n" +
			"    keystore-security-provider: \"JKS\"\n" +
			"    password: \"password\"\n" +
			"    enabled: true\n";

	@Test
	public void ensureThatCanReadFileFromFileSystem() throws IOException {
		try ( InputStream file = SystemResource.openFile( "tests-resources/ssl-configuration.yml" ) ) {
			assertNotNull( file );
		}
	}

	@Test
	public void ensureThatCanReadFileFromClassPath() throws IOException {
		try ( InputStream file = SystemResource.openFile( "ssl-configuration.yml" ) ) {
			assertNotNull( file );
		}
	}

	@Test
	public void ensureCanReadAsStringFile(){
		final String readFile = SystemResource.readFileAsString( "ssl-configuration.yml", "UTF-8" );
		assertEquals( FILE_CONTENT, readFile );
	}
}