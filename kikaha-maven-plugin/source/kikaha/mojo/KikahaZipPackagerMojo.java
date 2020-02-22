package kikaha.mojo;

import kikaha.mojo.packager.ZipFileReader;
import kikaha.mojo.packager.ZipFileWriter;
import lombok.Cleanup;
import lombok.val;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;

@Mojo( name = "package",
		requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class KikahaZipPackagerMojo extends AbstractMojo {

	static final String DEFAULT_DIR = "META-INF/defaults/";
	static final String METAINF_DIR = "META-INF";

	@Parameter( defaultValue = "${project}", required = true )
	MavenProject project;

	@Parameter( defaultValue = "${project.basedir}/src/main/webapp", required = true )
	String webResourcesPath;

	@Parameter( defaultValue = "${project.build.directory}", required = true )
	File targetDirectory;

	@Parameter( defaultValue = "${project.build.resources[0].directory}", required = true )
	File resourceDirectory;

	@Parameter( defaultValue = "${project.build.finalName}", required = true )
	String finalName;

	@Parameter( defaultValue = "false", required = true)
	boolean force;

	@Parameter( defaultValue = "false", required = true)
	boolean enabled;

	@Component
	ArtifactResolver resolver;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if ( !enabled ) return;
		if ( !project.getPackaging().equals( "jar" ) && !force ) return;

		ensureTargetDirectoryExists();
		final ZipFileWriter zip = createZipFile();
		getLog().info( "Building application package: " + zip.getFileName());
		populateZip( zip );
		zip.close();
	}

	void ensureTargetDirectoryExists() throws MojoFailureException {
		if ( !targetDirectory.exists() )
			if ( !targetDirectory.mkdirs() )
				throw new MojoFailureException( "Target Directory not created" );
	}

	ZipFileWriter createZipFile() throws MojoExecutionException {
		final String fileName = targetDirectory.getAbsolutePath() + File.separatorChar + finalName + ".zip";
		final ZipFileWriter zipFile = new ZipFileWriter( fileName, finalName );
		zipFile.stripPrefix( DEFAULT_DIR, METAINF_DIR );
		return zipFile;
	}

	void populateZip( final ZipFileWriter zip ) throws MojoExecutionException {
		try {
			copyFilesFromPluginJarToZip( zip );
			copyDependenciesToZip( zip );
			copyFinalArtifactToZip( zip );
			copyWebResourceFolderToZip( zip );
			copyDirectoryFilesToZip( zip, resourceDirectory, "" );
		} catch ( final IOException | ArtifactResolutionException e ) {
			throw new MojoExecutionException( "Failed to populate zip", e );
		}
	}

	void copyFilesFromPluginJarToZip( final ZipFileWriter zip ) throws MojoExecutionException, IOException {
		final CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
		final URL location = codeSource.getLocation();
		final String jar = location.getPath();
		copyFilesFromJarToZip( zip, jar );
	}

	void copyFilesFromJarToZip( final ZipFileWriter zip, final String path ) throws MojoExecutionException, IOException {
		final ZipFileReader reader = new ZipFileReader( path.replace( "%20", " " ) );
		try {
			reader.read( ( name, content ) -> {
				if ( name.startsWith( DEFAULT_DIR ) )
					zip.add( name, content );
			} );
		} finally {
			reader.close();
		}
	}

	@SuppressWarnings( "unchecked" )
	void copyDependenciesToZip( final ZipFileWriter zip )
			throws MojoExecutionException, IOException, ArtifactResolutionException {
		final Set<String> namesAlreadyIncludedToZip = new HashSet<>();
		for ( final Artifact artifact : (Set<Artifact>)project.getArtifacts() ) {
			final String artifactAbsolutePath = getArtifactAbsolutePath( artifact );
			if ( !namesAlreadyIncludedToZip.contains( artifactAbsolutePath ) ) {
				copyDependencyToZip( zip, artifact, artifactAbsolutePath );
				namesAlreadyIncludedToZip.add( artifactAbsolutePath );
			}
		}
	}

	void copyDependencyToZip(
			final ZipFileWriter zip,
			final Artifact artifact,
			final String artifactAbsolutePath ) throws IOException, MojoExecutionException
	{
		if ( artifact.getScope().equals( "provided" ) )
			return;

		final String jarName = "lib/" + artifact.getArtifactId() + "." + artifact.getType();
		final InputStream inputStream = new FileInputStream( artifactAbsolutePath );
		zip.add( jarName, inputStream );
		copyFilesFromJarToZip( zip, artifactAbsolutePath );
	}

	private String getArtifactAbsolutePath(final Artifact artifact) throws ArtifactResolutionException {
		val projectBuildingRequest = project.getProjectBuildingRequest();

		val aetherArtifact = new DefaultArtifact(
				artifact.getGroupId(),
				artifact.getArtifactId(),
				artifact.getClassifier(),
				artifact.getVersion()
		);

		val request = new ArtifactRequest()
				.setArtifact(aetherArtifact)
				.setRepositories(project.getRemoteProjectRepositories())
				;
		resolver.resolveArtifact(projectBuildingRequest.getRepositorySession(), request);
		return artifact.getFile().getAbsolutePath();
	}

	void copyFinalArtifactToZip( final ZipFileWriter zip ) {
		try {
			final String fileName = String.format( "%s.%s", finalName, project.getPackaging() );
			final InputStream inputStream = new FileInputStream( new File( targetDirectory, fileName ) );
			zip.add( "lib/" + fileName, inputStream );
		} catch ( final FileNotFoundException cause ) {
			System.out.println( cause.getMessage() + ". Ignoring." );
		}
	}

	void copyWebResourceFolderToZip( final ZipFileWriter zip ) throws IOException
	{
		final File directory = new File(webResourcesPath);
		copyDirectoryFilesToZip( zip, directory, "webapp" );
	}

	void copyDirectoryFilesToZip( final ZipFileWriter zip, final File directory, final String rootDirectory ) throws IOException {
		if ( directory.exists() )
			for ( final File file : directory.listFiles() )
				copyToZip( zip, rootDirectory, file );
	}

	void copyToZip( final ZipFileWriter zip, final String rootDirectory, final File file ) throws IOException {
		final String fileName = ( rootDirectory + "/" + file.getName() ).replaceFirst( "^/", "" );
		if ( file.isDirectory() )
			copyDirectoryFilesToZip( zip, file, fileName );
		else
			copyFileToZip( zip, file, fileName );
	}

	void copyFileToZip( final ZipFileWriter zip, final File file, final String fileName ) throws IOException {
		@Cleanup final InputStream content = new FileInputStream( file );
		zip.add( fileName, content );
	}
}