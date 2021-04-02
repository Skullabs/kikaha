package kikaha.mojo;

import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.codedeploy.*;
import com.amazonaws.services.codedeploy.model.*;
import com.amazonaws.services.s3.*;
import kikaha.config.*;
import kikaha.core.util.Lang;
import kikaha.mojo.packager.*;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.*;

/**
 *
 */
@Mojo( name = "deploy-on-aws-s3", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class KikahaS3DeployerMojo extends AbstractMojo {

	static final String
		DEFAULT_CODEDEPLOY_DIR = "META-INF/aws-code-deploy/",
        DEFAULT_CONF_DIR = "conf/",
		HEALTH_CHECK_ENABLED = "server.health-check.enabled",
		HEALTH_CHECK_URL = "server.health-check.url",
		HTTPS_ENABLED = "server.https.enabled",
		HTTPS_PORT = "server.https.port",
		HTTPS_HOST = "server.https.host",
		HTTP_PORT = "server.http.port",
		HTTP_HOST = "server.http.host",
		DEFAULT_HOST = "0.0.0.0"
	;

	final AWSCredentialsProviderChain credentials = DefaultAWSCredentialsProviderChain.getInstance();

	@Parameter( defaultValue = "false", required = true )
	Boolean enabled;

	@Parameter( defaultValue = "false", required = true )
	Boolean useCodeDeploy;

	@Parameter( defaultValue = DEFAULT_CODEDEPLOY_DIR )
	String codeDeployFolder;

	@Parameter( defaultValue = "${project.groupId}-${project.artifactId}" )
	String codeDeployApplicationName;

	@Parameter( defaultValue = "production" )
	String codeDeployDeploymentGroupName;

	@Parameter( defaultValue = "" )
	String codeDeployValidationCommand;

	@Parameter( defaultValue = "120", required = true)
	Integer codeDeployWaitTime;

	@Parameter( defaultValue = "false", required = true )
	Boolean codeDeployIgnoreApplicationStopFailures;

	@Parameter( defaultValue = "${project.build.finalName}-runnable.jar", required = true )
	String jarFileName;

	@Parameter( defaultValue = "${project.build.directory}", required = true )
	File targetDirectory;

	@Parameter( defaultValue = "us-east-1", required = true )
	String regionName;

	@Parameter( required = true )
	String s3Bucket;

	@Parameter( defaultValue = "${project.groupId}-${project.artifactId}", required = true )
	String s3Key;

    final Set<String> alreadyInsertedFiles = new HashSet<>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if ( !enabled ) return;

		File packageFile = useCodeDeploy ? createCodeDeployZipFile() : getJarFile();
		if ( !packageFile.exists() )
			throw new MojoFailureException( "Package not found: " + packageFile.getName() + ". Try execute kikaha:jar and try again." );

		getLog().info( "Deploying package on AWS S3: " + s3Bucket + "/" + s3Key );
		uploadPackage( packageFile );
		if ( useCodeDeploy )
			deployPackage();
	}

	void uploadPackage( File packageFile ) {
		final AmazonS3 s3 = AmazonS3Client.builder().withCredentials( credentials )
				.withRegion( Regions.fromName(regionName) ).build();
		s3.putObject( s3Bucket, s3Key, packageFile );
	}

	void deployPackage() {
		final AmazonCodeDeploy codeDeploy = AmazonCodeDeployClient.builder().withCredentials(credentials)
				.withRegion(Regions.fromName(regionName)).build();
		final S3Location s3Location = new S3Location().withBucket(s3Bucket).withKey(s3Key)
				.withBundleType(BundleType.Zip);
		final CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest()
				.withApplicationName( codeDeployApplicationName )
				.withDeploymentGroupName( codeDeployDeploymentGroupName )
				.withIgnoreApplicationStopFailures( codeDeployIgnoreApplicationStopFailures )
				.withRevision(new RevisionLocation().withS3Location(s3Location).withRevisionType(RevisionLocationType.S3));
		final CreateDeploymentResult result = codeDeploy.createDeployment(createDeploymentRequest);
		getLog().info( result.toString() );
	}

	File getJarFile(){
		return new File( targetDirectory.getAbsolutePath() + File.separatorChar + jarFileName );
	}

	File createCodeDeployZipFile() throws MojoExecutionException {
		final String fileName = targetDirectory.getAbsolutePath() + File.separatorChar + s3Key + ".zip";
		final File file = new File( fileName );
		getLog().info( "Creating deployment package for AWS CodeDeploy at " + file.getAbsolutePath() );

		final ZipFileWriter zipFile = createZipFile(fileName);
		copyFileToZip( zipFile, getJarFile(), "lib/application.jar" );
		getLog().info( "Adding 'conf' files to your packages..." );
        copyFilesFromJarToZip( zipFile, getJarFile().getAbsolutePath() );

		if ( DEFAULT_CODEDEPLOY_DIR.equals(codeDeployFolder) )
			copyFilesFromPluginJarToZip(zipFile);
		else
			copyCodeDeployFolderFolderToZip( zipFile );

		zipFile.close();
		return file;
	}

	ZipFileWriter createZipFile( final String fileName ) throws MojoExecutionException {
		final ZipFileWriter zipFile = new ZipFileWriter( fileName );
		zipFile.stripPrefix( codeDeployFolder );
		return zipFile;
	}

	void copyFilesFromPluginJarToZip( final ZipFileWriter zip ) throws MojoExecutionException {
		final String jar = getMavenPluginJarLocation();
		copyFilesFromJarToZip( zip, jar );
	}

	CodeDeployFilesParser codeDeployFilesParser() throws MojoExecutionException {
		final Config config = JarFileConfigReader.read(getJarFile()).getConfig();
		final boolean healthCheckEnabled = config.getBoolean( HEALTH_CHECK_ENABLED );
		final String url = healthCheckEnabled ? config.getString( HEALTH_CHECK_URL ) : "/";
		final boolean httpsEnabled = config.getBoolean( HTTPS_ENABLED );
		final int port = config.getInteger( httpsEnabled ? HTTPS_PORT : HTTP_PORT );
		final String host = config.getString( httpsEnabled ? HTTPS_HOST : HTTP_HOST )
				.replace( DEFAULT_HOST,"localhost" );
		final String scheme = httpsEnabled ? "https" : "http";
		final String curl = healthCheckEnabled ? "curl -f " : "curl ";
		final String validationCommand = Lang.isUndefined( codeDeployValidationCommand )
				? curl + scheme + "://" + host + ":" + port + url : codeDeployValidationCommand;
		return CodeDeployFilesParser.of( scheme, host, port, url, validationCommand, codeDeployWaitTime );
	}

	void copyFilesFromJarToZip( final ZipFileWriter zip, final String path ) throws MojoExecutionException {
		final CodeDeployFilesParser parser = codeDeployFilesParser();
		try ( final ZipFileReader reader = new ZipFileReader( path.replace( "%20", " " ) ) ) {
			reader.read((name, content) -> {
				if ( !alreadyInsertedFiles.contains( name ) && name.startsWith(DEFAULT_CONF_DIR) || name.startsWith(DEFAULT_CODEDEPLOY_DIR)) {
				    alreadyInsertedFiles.add( name );
				    zip.add(name, parser.parse(content));
                }
			});
		} catch ( IOException cause ) {
			throw new MojoExecutionException( "Can't copy file to zip file", cause );
		}
	}

	void copyCodeDeployFolderFolderToZip( final ZipFileWriter zip ) throws MojoExecutionException {
		final File directory = new File(codeDeployFolder);
		copyDirectoryFilesToZipPreservingRelativePaths( zip, directory, directory );
	}

	void copyDirectoryFilesToZipPreservingRelativePaths( final ZipFileWriter zip, final File rootDir, final File directory ) throws MojoExecutionException {
		if ( directory.exists() )
			for ( final File file : directory.listFiles() )
				copyFileToZip( zip, rootDir, file );
	}

	void copyFileToZip(final ZipFileWriter zip, final File rootDir, final File file ) throws MojoExecutionException {
		if ( file.isDirectory() )
			copyDirectoryFilesToZipPreservingRelativePaths( zip, rootDir, file );
		else {
			final String fileName = file.getAbsolutePath()
					.replace( rootDir.getAbsolutePath(), "" )
					.replaceFirst( "^/", "" )
					.replaceFirst( "^\\\\", "" );
			copyFileToZip( zip, file, fileName );
		}
	}

	void copyFileToZip( final ZipFileWriter zip, final File file, final String fileName ) throws MojoExecutionException {
		try {
			final InputStream content = new FileInputStream(file);
			zip.add(fileName, content);
		} catch ( IOException cause ) {
			throw new MojoExecutionException( "Failed to copy file to zip", cause );
		}
	}

	String getMavenPluginJarLocation(){
		final CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
		final URL location = codeSource.getLocation();
		return location.getPath();
	}

	static String readAsString( InputStream file ) {
		try {
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			copy( file, output );
			return output.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	static void copy( InputStream from, OutputStream to ) {
		try {
			final byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = from.read(buffer)) >= 0)
				to.write(buffer, 0, len);
		} catch (IOException e) {
			throw new IllegalStateException( e );
		}
	}
}

@RequiredArgsConstructor(staticName = "read")
class JarFileConfigReader {

	final MergeableConfig mergeableConfig = MergeableConfig.create();
	final File jarFile;

	Config getConfig() throws MojoExecutionException {
		readJarFile( "META-INF/defaults.yml" );
		readJarFile( "conf/application.yml" );
		return mergeableConfig;
	}

	private void readJarFile(String fileName) throws MojoExecutionException {
		try ( final ZipFileReader reader = new ZipFileReader( jarFile.getAbsolutePath() ) ) {
			reader.read( (name, content) -> {
				if ( name.endsWith( fileName ) )
					mergeableConfig.load( content );
			});
		} catch ( IOException cause ) {
			throw new MojoExecutionException( "Failed to copy file to zip", cause );
		}
	}


}

@RequiredArgsConstructor( staticName = "of" )
class CodeDeployFilesParser {

	final String scheme;
	final String host;
	final Integer port;
	final String healthCheckUrl;
	final String validationCommand;
	final Integer codeDeployWaitTime;

	InputStream parse(InputStream content){
		final String contentAsString = KikahaS3DeployerMojo.readAsString( content )
				.replace("{{scheme}}", scheme)
				.replace("{{host}}", host)
				.replace("{{port}}", port.toString())
				.replace("{{health-check-url}}", healthCheckUrl)
				.replace("{{validation-command}}", validationCommand)
				.replace("{{code-deploy-validate-wait-time}}", codeDeployWaitTime.toString());
		return new ByteArrayInputStream( contentAsString.getBytes() );
	}

}