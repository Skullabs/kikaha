package kikaha.urouting.apt;

import static java.lang.String.format;
import static kikaha.apt.APT.*;
import static kikaha.urouting.apt.MicroRoutingParameterParser.extractHttpPathFrom;
import static kikaha.urouting.apt.MicroRoutingParameterParser.extractResponseContentTypeFrom;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import javax.annotation.processing.*;
import javax.enterprise.inject.Typed;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import kikaha.apt.*;
import kikaha.urouting.RoutingMethodData;
import kikaha.urouting.api.*;
import lombok.Getter;

@Getter
@SupportedAnnotationTypes( "kikaha.urouting.api.*" )
public class MicroRoutingAnnotationProcessor extends AbstractAnnotatedMethodProcessor {

	final MethodParametersExtractor parametersExtractor = new MicroRoutingParameterParser(
		this::extractParamFromNonAnnotatedParameter
	);

	final List<Class<? extends Annotation>> expectedMethodAnnotations = Arrays.asList( GET.class, POST.class, PUT.class, DELETE.class, PATCH.class, MultiPartFormData.class );
	final String templateName = "routing-method-class.mustache";

	@Override
	public void generateMethod( final ExecutableElement method, final RoundEnvironment roundEnv,
			final Class<? extends Annotation> httpMethodAnnotation ) throws IOException {
		final RoutingMethodData routingMethodData = toRoutingMethodData( method, httpMethodAnnotation );
		info( "  Ф " + routingMethodData );
		generator.generate( routingMethodData );
	}

	private RoutingMethodData toRoutingMethodData(
			final ExecutableElement method,
			final Class<? extends Annotation> httpMethodAnnotation )
	{
		final String type = asType( method.getEnclosingElement() ),
				methodParams = parametersExtractor.extractMethodParamsFrom( method );
		final boolean isMultiPart = httpMethodAnnotation.equals( MultiPartFormData.class ) || methodParams.contains( "methodDataProvider.getFormParam" ),
				isAsyncMode = methodParams.contains( "asyncResponse" );
		final String httpMethod = httpMethodAnnotation.equals( MultiPartFormData.class ) ? "POST" : httpMethodAnnotation.getSimpleName();
		return createRouteMethodData( method, isMultiPart, httpMethod, type, methodParams, isAsyncMode );
	}

	private static RoutingMethodData createRouteMethodData(
			final ExecutableElement method, final boolean isMultiPart,
			final String httpMethod, final String type,
			final String methodParams, final boolean isAsyncMode )
	{
		final String returnType = extractReturnTypeFrom( method );
		final boolean requiresBodyData = methodParams.contains( "bodyData" );

		if ( returnType != null && isAsyncMode )
			throw new UnsupportedOperationException( "Invalid Routing Method '" + method.asType().toString() +"'. Async methods should not have return type." );

		return new RoutingMethodData(
				extractTypeName( type ), extractPackageName( type ), method.getSimpleName().toString(),
				methodParams, returnType, extractResponseContentTypeFrom( method ),
				extractHttpPathFrom( method ), httpMethod, extractServiceInterfaceFrom( method ),
				requiresBodyData, isMultiPart, isAsyncMode );
	}

	private String extractParamFromNonAnnotatedParameter( ExecutableElement method, VariableElement parameter ) {
		final String
				consumingContentType = extractConsumingContentTypeFrom( method ),
				targetType = asType( parameter );

		if ( consumingContentType != null )
			return format( "methodDataProvider.getBody( exchange, %s.class, bodyData, \"%s\" )", targetType, consumingContentType );
		return format( "methodDataProvider.getBody( exchange, %s.class, bodyData )", targetType );
	}

	static String extractConsumingContentTypeFrom( final ExecutableElement method ) {
		Consumes consumesAnnotation = method.getAnnotation( Consumes.class );
		if ( consumesAnnotation == null )
			consumesAnnotation = method.getEnclosingElement().getAnnotation( Consumes.class );
		if ( consumesAnnotation != null )
			return consumesAnnotation.value();
		return null;
	}

	static String extractServiceInterfaceFrom( final ExecutableElement method ) {
		final TypeElement classElement = (TypeElement)method.getEnclosingElement();
		final String canonicalName = getServiceInterfaceProviderClass( classElement ).toString();
		if ( canonicalName.isEmpty() )
			return classElement.asType().toString();
		return canonicalName;
	}

	static TypeMirror getServiceInterfaceProviderClass( final TypeElement service ) {
		try {
			final Typed annotation = service.getAnnotation(Typed.class);
			if ( annotation != null )
				annotation.value()[0].getCanonicalName();
			return new EmptyTypeMirror();
		} catch ( final MirroredTypesException cause ) {
			return cause.getTypeMirrors().get(0);
		}
	}
}
