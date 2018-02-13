package kikaha.mojo.packager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static kikaha.mojo.packager.packager.*;

/**
 * Created by miere.teixeira on 05/12/2016.
 */
@Getter
@RequiredArgsConstructor
public class JarWriter {

    final Set<String> jarFiles = new HashSet<>();
    final ZipOutputStream output;
    final Map<String, FileMerger> mergers;
    final String fileName;
    final List<Pattern> filterPatterns;

    public JarWriter( final String fileName, final List<String> filterPatterns ) throws FileNotFoundException {
        mergers = new HashMap<>();
        mergers.put( "META-INF/MANIFEST.MF", new ManifestMerger( "META-INF/MANIFEST.MF" ) );
        mergers.put( "META-INF/defaults.yml", new YmlConfigMerger( "META-INF/defaults.yml" ) );
        mergers.put( "conf/application.yml", new YmlConfigMerger( "conf/application.yml" ) );
        this.output = new ZipOutputStream( new FileOutputStream( fileName ) );
        this.fileName = fileName;
        this.filterPatterns = filterPatterns.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    public void mergeJar(final String path) throws MojoExecutionException {
        try (final ZipFileReader reader = new ZipFileReader(path.replace("%20", " "))) {
            try {
                reader.read( this::addFile );
            } catch (IOException e) {
                throw new MojoExecutionException( MESSAGE_CANT_ADD_TO_ZIP, e );
            }
        }
    }

    public void addFile( final String name, final InputStream content ) {
        try {
            if ( shouldIgnoreFile(name) ) return;
            final FileMerger writable = mergers.computeIfAbsent( name,
                n-> n.startsWith(SERVICE_FILE_NAME) ? new SimpleMerger(n) : null );
            if ( writable != null )
               writable.add( content );
            else if ( !jarFiles.contains( name ) )
                add( name, content );
        } catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    public void add( final String name, final InputStream content ) {
        try {
            output.putNextEntry( new ZipEntry( name ) );
            copy( content, output::write );
            output.closeEntry();
            jarFiles.add( name );
        } catch ( final IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( MESSAGE_CANT_ADD_TO_ZIP + " ("+name+")", e );
        }
    }

    public void flush() throws MojoExecutionException {
        try {
            for (final FileMerger merger : mergers.values()) {
                final String merged = merger.merge();
                addMergedFile(merger.getFileName(), merged);
            }
            output.close();
        } catch (IOException e) {
            throw new MojoExecutionException( MESSAGE_CANT_ADD_TO_ZIP, e );
        }
    }

    void addMergedFile( final String name, final String content ) {
        try {
            output.putNextEntry( new ZipEntry( name ) );
            final byte[] bytes = content.getBytes();
            output.write( bytes, 0, bytes.length );
            output.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException( MESSAGE_CANT_ADD_TO_ZIP + " ("+name+")", e );
        }
    }

    private boolean shouldIgnoreFile( String name ) {
        for ( Pattern re : this.filterPatterns )
            if ( re.matcher(name).matches() ) {
                System.out.println( "Ignoring " + name );
                return true;
            }
        return false;
    }
}
