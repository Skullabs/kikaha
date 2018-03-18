package kikaha.cloud.aws.lambda;

import java.util.*;

import kikaha.commons.Cookie;
import kikaha.commons.Cookies;
import kikaha.core.cdi.helpers.TinyList;
import kikaha.urouting.api.Headers;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * With the Lambda proxy integration, API Gateway maps the entire client request to the
 * input event parameter of the back-end Lambda function as defined on this class.
 */
@Accessors( chain = true )
@ToString(exclude = "cookies")
@SuppressWarnings("unchecked")
public class AmazonHttpRequest {

    @Setter @Getter String resource;
    @Setter @Getter String path;
    @Setter @Getter String httpMethod;
    @Setter @Getter Map<String, String> headers;
    @Setter @Getter Map<String, String> queryStringParameters;
    @Setter @Getter Map<String, String> pathParameters;
    @Setter @Getter Map<String, String> stageVariables;
    @Setter @Getter RequestContext requestContext;
    @Setter @Getter String body;
    @Setter @Getter boolean isBase64Encoded;

	@Getter(lazy = true)
	private final Map<String, Cookie> cookies = parseCookies();

    private Map<String, Cookie> parseCookies() {
		final String cookie = headers.get( Headers.COOKIE );
		if (cookie == null)
			return Collections.emptyMap();
		return Cookies.parseRequestCookies( 50, false, TinyList.singleElement( cookie ) );
	}

	public Map<String, String> getPathParameters(){
        if ( pathParameters == null )
            pathParameters = new HashMap<>();
        return pathParameters;
    }

    public String getContentType() {
        return headers.get( Headers.CONTENT_TYPE );
    }

    @Getter
	@Setter
	@ToString
	public static class RequestContext {
		String accountId;
		String resourceId;
		String stage;
		String requestId;
		RequestContextIdentity identity;
		String resourcePath;
		String httpMethod;
		String apiId;
	}

	@Getter
	@Setter
	@ToString
	public static class RequestContextIdentity {
		String cognitoIdentityPoolId;
		String accountId;
		String cognitoIdentityId;
		String caller;
		String apiKey;
		String sourceIp;
		String cognitoAuthenticationType;
		String cognitoAuthenticationProvider;
		String userArn;
		String userAgent;
		String user;
	}
}
