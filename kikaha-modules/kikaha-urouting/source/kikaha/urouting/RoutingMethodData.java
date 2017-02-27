package kikaha.urouting;

import static java.lang.String.format;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Typed;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import kikaha.core.cdi.Stateless;
import kikaha.urouting.api.AsyncResponse;
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
import lombok.val;

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
	final boolean requiresBodyData;
	final boolean requiresFormData;
	final boolean asyncMode;

	@Getter( lazy = true )
	private final Long identifier = createIdentifier();

	private Long createIdentifier() {
		return hashCode() & 0xffffffffl;
	}

	@Override
	public String toString() {
		return format( "%-55s -> %s:%s ",
			getType() + "." + getMethodName(), getHttpMethod(), getHttpPath() );
	}

	public static RoutingMethodData from(
		final ExecutableElement method,
		final Class<? extends Annotation> httpMethodAnnotation )
	{
		val type = method.getEnclosingElement().asType().toString();
		val methodParams = extractMethodParamsFrom( method );
		val isMultiPart = httpMethodAnnotation.equals( MultiPartFormData.class )
			|| methodParams.contains( "methodDataProvider.getFormParam" );
		val isAsyncMode = methodParams.contains( "asyncResponse" );
		val httpMethod = isMultiPart
			? "POST" : httpMethodAnnotation.getSimpleName();
		return createRouteMethodData( method, isMultiPart, httpMethod, type, methodParams, isAsyncMode );
	}

	private static RoutingMethodData createRouteMethodData(
		final ExecutableElement method, final boolean isMultiPart,
		final String httpMethod, final String type,
		final String methodParams, final boolean isAsyncMode )
	{
		val returnType = extractReturnTypeFrom( method );
		val requiresBodyData = methodParams.contains( "methodDataProvider.getBody" );

		if ( returnType != null && isAsyncMode )
			throw new UnsupportedOperationException( "Invalid Routing Method '" + method.asType().toString() +"'. Async methods should not have return type." );

		return new RoutingMethodData(
			type, extractPackageName( type ), method.getSimpleName().toString(),
			methodParams, returnType, extractResponseContentTypeFrom( method ),
			measureHttpPathFrom( method ), httpMethod, extractServiceInterfaceFrom( method ),
			requiresBodyData, isMultiPart, isAsyncMode );
	}

	public static String extractPackageName( final String canonicalName )
	{
		return canonicalName.replaceAll( "^(.*)\\.[^.]+", "$1" );
	}

	static String extractReturnTypeFrom( final ExecutableElement method ) {
		val returnTypeAsString = method.getReturnType().toString();
		if ( "void".equals( returnTypeAsString ) )
			return null;
		return returnTypeAsString;
	}

	static String extractMethodParamsFrom( final ExecutableElement method ) {
		val buffer = new StringBuilder().append( METHOD_PARAM_EOL );
		boolean first = true;
		for ( val parameter : method.getParameters() ) {
			if ( !first )
				buffer.append( ',' );
			buffer.append( extractMethodParamFrom( method, parameter ) ).append( METHOD_PARAM_EOL );
			first = false;
		}
		return buffer.toString();
	}

	/**
	 * Extract method parameter for a given {@link VariableElement} argument.
	 * The returned method parameter will be passed as argument to a routing
	 * method.
	 *
	 * @param method
	 * @param parameter
	 * @return
	 */
	// XXX: bad, ugly and huge method
	static String extractMethodParamFrom( final ExecutableElement method, final VariableElement parameter ) {
		val targetType = parameter.asType().toString().replaceAll("<[^>]+>","");
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
			return getParam( FormParam.class, formParam.value(), targetType );
		if ( AsyncResponse.class.getCanonicalName().equals( targetType ) )
			return format( "asyncResponse" );
		Context dataParam = parameter.getAnnotation( Context.class );
		if ( dataParam != null )
			return format( "methodDataProvider.getData( exchange, %s.class )", targetType );
		return getBodyParam( method, targetType );
	}

	static String getParam( final Class<?> targetAnnotation, final String param, final String targetType ) {
		return format( "methodDataProvider.get%s( exchange, \"%s\", %s.class )",
				targetAnnotation.getSimpleName(), param, targetType );
	}

	static String getBodyParam( final ExecutableElement method, final String targetType ) {
		final String consumingContentType = extractConsumingContentTypeFrom( method );
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

	static String extractResponseContentTypeFrom( final ExecutableElement method ) {
		Produces producesAnnotation = method.getAnnotation( Produces.class );
		if ( producesAnnotation == null )
			producesAnnotation = method.getEnclosingElement().getAnnotation( Produces.class );
		if ( producesAnnotation != null )
			return producesAnnotation.value();
		return null;
	}

	static String measureHttpPathFrom( final ExecutableElement method ) {
		val classElement = method.getEnclosingElement();
		Path pathAnnotationOfClass = classElement.getAnnotation( Path.class );
		val rootPath = pathAnnotationOfClass != null ? pathAnnotationOfClass.value() : "/";
		Path pathAnnotationOfMethod = method.getAnnotation( Path.class );
		val methodPath = pathAnnotationOfMethod != null ? pathAnnotationOfMethod.value() : "/";
		return generateHttpPath( rootPath, methodPath );
	}

	public static String generateHttpPath( final String rootPath, final String methodPath ) {
		return String.format( "/%s/%s/", rootPath, methodPath )
				.replaceAll( "//+", "/" );
	}

	static String extractServiceInterfaceFrom( final ExecutableElement method ) {
		val classElement = (TypeElement)method.getEnclosingElement();
		val canonicalName = getServiceInterfaceProviderClass( classElement ).toString();
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
