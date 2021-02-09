package kikaha.mojo.packager;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ZipFileWriterTest {

    @Test
    public void testFixEntryName() throws MojoExecutionException {
        //Testing special regex chars like \k, \Q, \E and others
        final String[] prefixesToTest = {"c:\\kikaha-test\\app", "d:\\Qope-test\\app", "l:\\Eoad-test\\app", "/Quit/test", "/kikaha-tes/c/app"};
        final String additionalPath = "/conf/application.yaml";
        final ZipFileWriter zipFileWriter = new ZipFileWriter("output/generated-test.zip");
        zipFileWriter.stripPrefix(prefixesToTest);

        for (String prefix : prefixesToTest)
            assertEquals(additionalPath, zipFileWriter.fixEntryName(prefix + additionalPath));

    }
}