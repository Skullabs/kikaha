package kikaha.mojo;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import kikaha.mojo.packager.ManifestMerger;
import org.junit.Test;

/**
 * Created by miere.teixeira on 05/12/2016.
 */
public class ManifestMergerTest {

    final String expectedMergedManifest = "Manifest-Version: 1.0\r\n" +
            "Created-By: Kikaha 2.2.0-SNAPSHOT\r\n" +
            "Main-Class: kikaha.core.cdi.ApplicationRunner\r\n" +
            "\r\n";

    @Test
    public void ensureCanMergeManifestFiles() throws IOException {
        final ManifestMerger merger = new ManifestMerger( "META-INF/MANIFEST.MF" );
        merger.add( getClass().getResourceAsStream( "/MANIFEST1.MF" ) );
        merger.add( getClass().getResourceAsStream( "/MANIFEST2.MF" ) );

        final String merged = merger.merge();
        assertEquals( expectedMergedManifest, merged );
    }
}
