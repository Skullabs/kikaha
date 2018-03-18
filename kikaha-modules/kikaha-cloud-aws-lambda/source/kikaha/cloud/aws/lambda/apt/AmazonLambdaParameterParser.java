package kikaha.cloud.aws.lambda.apt;

import static java.lang.String.format;
import static kikaha.apt.APT.*;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import javax.lang.model.element.*;
import kikaha.apt.*;
import kikaha.urouting.api.*;

/**
 *
 */
public class AmazonLambdaParameterParser extends MethodParametersExtractor {

	public AmazonLambdaParameterParser() {
		super( createAnnotationRules(), AmazonLambdaParameterParser::extractParamFromNonAnnotatedParameter );
	}

	static ChainedRules<VariableElement, Function<VariableElement, String>> createAnnotationRules() {
		final ChainedRules<VariableElement, Function<VariableElement, String>> rules = new ChainedRules<>();
		rules
			.with( isAnnotatedWith( PathParam.class ), v -> getParam( PathParam.class, v.getAnnotation( PathParam.class ).value(), v ) )
			.and( isAnnotatedWith( QueryParam.class ), v -> getParam( QueryParam.class, v.getAnnotation( QueryParam.class ).value(), v ) )
			.and( isAnnotatedWith( HeaderParam.class ), v -> getParam( HeaderParam.class, v.getAnnotation( HeaderParam.class ).value(), v ) )
			.and( isAnnotatedWith( CookieParam.class ), v -> getParam( CookieParam.class, v.getAnnotation( CookieParam.class ).value(), v ) )
			.and( APT.isAnnotatedWith( Context.class ), AmazonLambdaParameterParser::getContextParam)
			.and( APT.isAnnotatedWith( FormParam.class ), v -> throwsUnsupportedAnnotation( FormParam.class ) )
			.and( typeIs( AsyncResponse.class ), v -> throwsUnsupportedType( AsyncResponse.class ) )
		;
		return rules;
	}

	static String getParam( final Class<?> targetAnnotation, final String param, final VariableElement parameter ) {
		final String targetType = asType( parameter );
		return format( "methodDataProvider.get%s( request, \"%s\", %s.class )",
				targetAnnotation.getSimpleName(), param, targetType );
	}

	static String getContextParam( final VariableElement parameter ) {
		final String targetType = asType( parameter );
		return format( "methodDataProvider.getContextParam( request, %s.class )", targetType );
	}

	static String throwsUnsupportedAnnotation( final Class<? extends Annotation> clazz ) {
		throw new UnsupportedOperationException( "Annotation not supported by Lambda Functions: " + clazz );
	}

	static String throwsUnsupportedType( final Class<?> clazz ) {
		throw new UnsupportedOperationException( "Type not supported by Lambda Functions: " + clazz );
	}

	static String extractParamFromNonAnnotatedParameter( ExecutableElement method, VariableElement parameter ) {
		final String targetType = asType( parameter );
		return format( "methodDataProvider.getBody( request, %s.class )", targetType );
	}
}
