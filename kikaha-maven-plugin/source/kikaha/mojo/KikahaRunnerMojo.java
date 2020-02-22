package kikaha.mojo;

import kikaha.core.cdi.ApplicationRunner;
import kikaha.mojo.runner.MainClassService;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mojo( name = "run",
	requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class KikahaRunnerMojo extends AbstractMojo {

	@Parameter( defaultValue = "${project}", required = true)
	MavenProject project;

	@Parameter( defaultValue = "${project.basedir}/src/main/webapp", alias = "webResourcesPath", required = true)
	String webResourcesPath;

	@Parameter( defaultValue = "${localRepository}", required = true)
	ArtifactRepository localRepository;

	@Component
	ArtifactResolver resolver;

	@Parameter( defaultValue = "false", required = true)
	public boolean force;

	@Parameter( defaultValue = "-Xmx256m", required = true)
	String jvmArgs;

	@Parameter( defaultValue = "${project.build.outputDirectory}", required = true)
	File compiledClassesDir;

	@Parameter( defaultValue = "${project.basedir}", required = true)
	File baseDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if ( !project.getPackaging().equals( "jar" ) && !force ) return;
		try {
			val classpath = memorizeClassPathWithRunnableJar();
			val log = getLog();
			val service = new MainClassService( compiledClassesDir, ApplicationRunner.class.getCanonicalName(), classpath,
					asList( "-Dserver.static.location=" + absolutePath(webResourcesPath) ), getJvmArgs(), log );
			val process = service.start();
			if ( process.waitFor() > 0 )
				throw new RuntimeException( "Kikaha has unexpectedly finished." );
		} catch ( final Exception e ) {
			throw new MojoExecutionException( "Can't initialize Kikaha.", e );
		}
	}

	String getJvmArgs(){
		return jvmArgs.replaceAll("[\\n\\r]+", "").replaceAll("[\\t ]+"," ").trim();
	}

	String absolutePath( String str ) {
		final String absolutePath = new File(str).getAbsolutePath();
		return absolutePath;
	}

	List<String> asList( final String...strings ){
		val list = new ArrayList<String>();
		for ( val string : strings )
			list.add( string );
		return list;
	}

	@SuppressWarnings( "unchecked" )
	List<String> memorizeClassPathWithRunnableJar()
			throws ArtifactResolutionException {
		val artifactsInClassPath = new ArrayList<String>();
		for ( val artifact : (Set<Artifact>)this.project.getArtifacts() ) {
			val artifactAbsolutePath = getArtifactAbsolutePath( artifact );
			if ( !artifactsInClassPath.contains( artifactAbsolutePath ) )
				artifactsInClassPath.add( artifactAbsolutePath );
		}
		artifactsInClassPath.add( compiledClassesDir.getAbsolutePath() );
		artifactsInClassPath.add( baseDir.getAbsolutePath() );
		return artifactsInClassPath;
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
}