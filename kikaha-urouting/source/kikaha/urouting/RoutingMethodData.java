package kikaha.urouting;

import static java.lang.String.format;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import kikaha.urouting.api.Consumes;
import kikaha.urouting.api.Context;
import kikaha.urouting.api.CookieParam;
import kikaha.urouting.api.FormParam;
import kikaha.urouting.api.HeaderParam;
import kikaha.urouting.api.MultiPartFormData;
import kikaha.urouting.api.Path;
import kikaha.urouting.api.PathParam;
import kikaha.urouting.api.Produces;
import kikaha.urouting.api.QueryParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import trip.spi.Singleton;
import trip.spi.Stateless;

@Getter
@EqualsAndHashCode( exclude = "identifier" )
@RequiredArgsConstructor
public class RoutingMethodData {

	final static String METHOD_PARAM_EOL = "\n\t\t\t";

	final String type;
	final String packageName;
	final String methodName;
	final String methodParams;
	final String returnType;
	final String responseContentType;
	final String httpPath;
	final String httpMethod;
	final String serviceInterface;
	final boolean hasIOBound;
	final boolean isMultiPart;

	@Getter( lazy = true )
	private final Long identifier = createIdentifier();

	private Long createIdentifier() {
		return hashCode() & 0xffffffffl;
	}

	public static RoutingMethodData from(
			ExecutableElement method, Class<? extends Annotation> httpMethodAnnotation ) {
		boolean isMultiPart = httpMethodAnnotation.equals( MultiPartFormData.class );
		final String httpMethod = isMultiPart
				? "POST" : httpMethodAnnotation.getSimpleName();
		final String type = method.getEnclosingElement().asType().toString();
		final String methodParams = extractMethodParamsFrom( method );
		return new RoutingMethodData(
				type, extractPackageName( type ),
				method.getSimpleName().toString(),
			methodParams,
				extractReturnTypeFrom( method ),
				extractResponseContentTypeFrom( method ),
				measureHttpPathFrom( method ),
				httpMethod,
				extractServiceInterfaceFrom( method ),
			hasIOBlockingOperations( methodParams ),
				isMultiPart );
	}

	private static boolean hasIOBlockingOperations( final String methodParams ) {
		return methodParams.contains( "methodDataProvider.getBody" )
			|| methodParams.contains( "methodDataProvider.getFormParam" );
	}

	public static String extractPackageName( String canonicalName ) {
		return canonicalName.replaceAll( "^(.*)\\.[^\\.]+", "$1" );
	}

	static String extractReturnTypeFrom( ExecutableElement method ) {
		String returnTypeAsString = method.getReturnType().toString();
		if ( "void".equals( returnTypeAsString ) )
			return null;
		return returnTypeAsString;
	}

	static String extractMethodParamsFrom( ExecutableElement method ) {
		final StringBuilder buffer = new StringBuilder().append( METHOD_PARAM_EOL );
		boolean first = true;
		for ( VariableElement parameter : method.getParameters() ) {
			if ( !first )
				buffer.append( ',' );
			buffer.append( extractMethodParamFrom( method, parameter ) ).append( METHOD_PARAM_EOL );
			first = false;
		}
		return buffer.toString();
	}

	/**
	 * Extract method parameter for a given {@VariableElement}
	 * argument. The returned method parameter will be passed as argument to a
	 * routing method.
	 * 
	 * @param method
	 * @param parameter
	 * @return
	 */
	// XXX: bad, ugly and huge method
	static String extractMethodParamFrom( ExecutableElement method, VariableElement parameter ) {
		String targetType = parameter.asType().toString();
		PathParam pathParam = parameter.getAnnotation( PathParam.class );
		if ( pathParam != null )
			return getParam( PathParam.class, pathParam.value(), targetType );
		QueryParam queryParam = parameter.getAnnotation( QueryParam.class );
		if ( queryParam != null )
			return getParam( QueryParam.class, queryParam.value(), targetType );
		HeaderParam headerParam = parameter.getAnnotation( HeaderParam.class );
		if ( headerParam != null )
			return getParam( HeaderParam.class, headerParam.value(), targetType );
		CookieParam cookieParam = parameter.getAnnotation( CookieParam.class );
		if ( cookieParam != null )
			return getParam( CookieParam.class, cookieParam.value(), targetType );
		FormParam formParam = parameter.getAnnotation( FormParam.class );
		if ( formParam != null )
			return getFormParam( formParam.value(), targetType );
		Context dataParam = parameter.getAnnotation( Context.class );
		if ( dataParam != null )
			return format( "methodDataProvider.getData( exchange, %s.class )", targetType );
		return getBodyParam( method, targetType );
	}

	static String getFormParam( String param, String targetType ) {
		return format( "methodDataProvider.getFormParam( formData, \"%s\", %s.class )",
				param, targetType );
	}

	static String getParam( Class<?> targetAnnotation, String param, String targetType ) {
		return format( "methodDataProvider.get%s( exchange, \"%s\", %s.class )",
				targetAnnotation.getSimpleName(), param, targetType );
	}

	static String getBodyParam( ExecutableElement method, String targetType ) {
		String consumingContentType = extractConsumingContentTypeFrom( method );
		if ( consumingContentType != null )
			return format( "methodDataProvider.getBody( exchange, %s.class, \"%s\" )", targetType, consumingContentType );
		return format( "methodDataProvider.getBody( exchange, %s.class )", targetType );
	}

	static String extractConsumingContentTypeFrom( ExecutableElement method ) {
		Consumes consumesAnnotation = method.getAnnotation( Consumes.class );
		if ( consumesAnnotation == null )
			consumesAnnotation = method.getEnclosingElement().getAnnotation( Consumes.class );
		if ( consumesAnnotation != null )
			return consumesAnnotation.value();
		return null;
	}

	static String extractResponseContentTypeFrom( ExecutableElement method ) {
		Produces producesAnnotation = method.getAnnotation( Produces.class );
		if ( producesAnnotation == null )
			producesAnnotation = method.getEnclosingElement().getAnnotation( Produces.class );
		if ( producesAnnotation != null )
			return producesAnnotation.value();
		return null;
	}

	static String measureHttpPathFrom( ExecutableElement method ) {
		final Element classElement = method.getEnclosingElement();
		final Path pathAnnotationOfClass = classElement.getAnnotation( Path.class );
		final String rootPath = pathAnnotationOfClass != null ? pathAnnotationOfClass.value() : "/";
		final Path pathAnnotationOfMethod = method.getAnnotation( Path.class );
		final String methodPath = pathAnnotationOfMethod != null ? pathAnnotationOfMethod.value() : "/";
		return generateHttpPath( rootPath, methodPath );
	}

	public static String generateHttpPath( String rootPath, String methodPath ) {
		return String.format( "/%s/%s/", rootPath, methodPath )
				.replaceAll( "//+", "/" );
	}

	static String extractServiceInterfaceFrom( ExecutableElement method ) {
		TypeElement classElement = (TypeElement)method.getEnclosingElement();
		String canonicalName = getServiceInterfaceProviderClass( classElement ).toString();
		if ( Singleton.class.getCanonicalName().equals( canonicalName )
				|| Stateless.class.getCanonicalName().equals( canonicalName ) )
			return classElement.asType().toString();
		return canonicalName;
	}

	static TypeMirror getServiceInterfaceProviderClass( TypeElement service ) {
		try {
			final Singleton singleton = service.getAnnotation( Singleton.class );
			if ( singleton != null )
				singleton.exposedAs();
			final Stateless stateless = service.getAnnotation( Stateless.class );
			if ( stateless != null )
				stateless.exposedAs();
			return service.asType();
		} catch ( MirroredTypeException cause ) {
			return cause.getTypeMirror();
		}
	}
}
