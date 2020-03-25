package kikaha.mojo.packager;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

public class ZipFileWriterTest {

    @Test
    public void testFixEntryName() throws MojoExecutionException {
        //Testing special regex chars like \k, \Q, \E and others
        String[] pathsToTest = {"c:\\kikaha-test\\app", "c:\\Qope-test\\app", "c:\\Eoad-test\\app", "/Quit/test", "/kikaha-tes/c/app"};
        ZipFileWriter zipFileWriter = new ZipFileWriter("output/generated-test.zip");
        zipFileWriter.stripPrefix(pathsToTest);

        for (String path : pathsToTest)
            zipFileWriter.fixEntryName(path);
    }
}