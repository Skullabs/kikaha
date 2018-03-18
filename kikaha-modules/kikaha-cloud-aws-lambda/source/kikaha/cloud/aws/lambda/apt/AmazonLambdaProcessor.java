package kikaha.cloud.aws.lambda.apt;

import static kikaha.apt.APT.*;

import javax.annotation.processing.*;
import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import kikaha.apt.AbstractAnnotatedMethodProcessor;
import kikaha.urouting.api.*;
import lombok.Getter;

/**
 *
 */
@Getter
@Singleton
@Typed( Processor.class )
@SupportedAnnotationTypes( "kikaha.urouting.api.*" )
public class AmazonLambdaProcessor extends AbstractAnnotatedMethodProcessor {

	final AmazonLambdaParameterParser parameterParser = new AmazonLambdaParameterParser();
	final List<Class<? extends Annotation>> expectedMethodAnnotations = Arrays.asList( GET.class, POST.class, PUT.class, DELETE.class, PATCH.class );
	final String templateName = "lambda-routing-method.mustache";

	@Override
	protected void generateMethod( ExecutableElement method, RoundEnvironment roundEnvironment, Class<? extends Annotation> httpMethodAnnotation ) throws IOException
	{
		final AmazonLambdaMethodData routingMethodData = asGenerableClass( method, httpMethodAnnotation );
		info( "  λ " + routingMethodData );
		generator.generate( routingMethodData );
	}

	private AmazonLambdaMethodData asGenerableClass( final ExecutableElement method,
	                                                 final Class<? extends Annotation> httpMethodAnnotation )
	{
		final String type = asType( method.getEnclosingElement() );
		return new AmazonLambdaMethodData(
			extractTypeName( type ), extractPackageName( type ),
			method.getSimpleName().toString(),
			parameterParser.extractMethodParamsFrom( method ),
			extractReturnTypeFrom( method ),
			extractHttpPathFrom( method ), httpMethodAnnotation.getSimpleName()
		);
	}

    static String extractHttpPathFrom( final ExecutableElement method ) {
        final Element classElement = method.getEnclosingElement();
        final Path pathAnnotationOfClass = classElement.getAnnotation( Path.class );
        final String rootPath = pathAnnotationOfClass != null ? pathAnnotationOfClass.value() : "/";
        final Path pathAnnotationOfMethod = method.getAnnotation( Path.class );
        final String methodPath = pathAnnotationOfMethod != null ? pathAnnotationOfMethod.value() : "/";
        return generateHttpPath( rootPath, methodPath );
    }

    static String generateHttpPath( final String rootPath, final String methodPath ) {
        return String.format( "/%s/%s/", rootPath, methodPath )
                .replaceAll( "//+", "/" );
    }
}
