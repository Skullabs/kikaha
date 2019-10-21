package kikaha.urouting.apt;

import static java.lang.String.format;
import static kikaha.apt.APT.*;

import java.io.InputStream;
import java.util.function.*;
import javax.lang.model.element.*;
import kikaha.apt.*;
import kikaha.urouting.api.*;

/**
 * @author: miere.teixeira
 */
public class MicroRoutingParameterParser extends MethodParametersExtractor {

	public MicroRoutingParameterParser( BiFunction<ExecutableElement, VariableElement, String> extractParamFromNonAnnotatedParameter ) {
		super( createAnnotationRules(), extractParamFromNonAnnotatedParameter );
	}

	static ChainedRules<VariableElement, Function<VariableElement, String>> createAnnotationRules(){
		final ChainedRules<VariableElement, Function<VariableElement, String>> rules = new ChainedRules<>();
		rules
		   .with( isAnnotatedWith( PathParam.class ), v -> getParam( PathParam.class, v.getAnnotation( PathParam.class ).value(), v ) )
			.and( isAnnotatedWith( QueryParam.class ), v -> getParam( QueryParam.class, v.getAnnotation( QueryParam.class ).value(), v ) )
			.and( isAnnotatedWith( HeaderParam.class ), v -> getParam( HeaderParam.class, v.getAnnotation( HeaderParam.class ).value(), v ) )
			.and( isAnnotatedWith( CookieParam.class ), v -> getParam( CookieParam.class, v.getAnnotation( CookieParam.class ).value(), v ) )
			.and( isAnnotatedWith( FormParam.class ), v -> getParam( FormParam.class, v.getAnnotation( FormParam.class ).value(), v ) )
			.and( isAnnotatedWith( Context.class ), v -> format( "methodDataProvider.getData( exchange, %s.class )", asType( v ) ) )
			.and( typeIs( InputStream.class ), v -> "new java.io.ByteArrayInputStream(bodyData)" )
			.and( typeIs( AsyncResponse.class ), v -> "asyncResponse" );
		return rules;
	}

	static String getParam( final Class<?> targetAnnotation, final String param, final VariableElement parameter ) {
		final String targetType = asType( parameter );
		return format( "methodDataProvider.get%s( exchange, \"%s\", %s.class )",
				targetAnnotation.getSimpleName(), param, targetType );
	}

	public static String extractHttpPathFrom( final ExecutableElement method ) {
		final Element classElement = method.getEnclosingElement();
		final Path pathAnnotationOfClass = classElement.getAnnotation( Path.class );
		final String rootPath = pathAnnotationOfClass != null ? pathAnnotationOfClass.value() : "/";
		final Path pathAnnotationOfMethod = method.getAnnotation( Path.class );
		final String methodPath = pathAnnotationOfMethod != null ? pathAnnotationOfMethod.value() : "/";
		return generateHttpPath( rootPath, methodPath );
	}

	public static String generateHttpPath( final String rootPath, final String methodPath ) {
		return String.format( "/%s/%s/", rootPath, methodPath )
				.replaceAll( "//+", "/" );
	}

	public static String extractResponseContentTypeFrom( final ExecutableElement method ) {
		Produces producesAnnotation = method.getAnnotation( Produces.class );
		if ( producesAnnotation == null )
			producesAnnotation = method.getEnclosingElement().getAnnotation( Produces.class );
		if ( producesAnnotation != null )
			return producesAnnotation.value();
		return null;
	}
}
