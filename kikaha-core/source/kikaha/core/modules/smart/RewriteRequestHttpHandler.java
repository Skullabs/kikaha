package kikaha.core.modules.smart;

import java.util.*;
import io.undertow.server.*;
import kikaha.commons.url.URLMatcher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RewriteRequestHttpHandler implements HttpHandler {

	final RequestMatcher requestMatcher;
	final URLMatcher targetPath;
	final HttpHandler next;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		final Map<String, String> properties = new HashMap<>();
		if ( requestMatcher.apply( exchange, properties ) )
			exchange.setRelativePath(targetPath.replace(properties));
		next.handleRequest(exchange);
	}

	public static HttpHandler from(final SmartRouteRule rule, final HttpHandler next )
	{
		return new RewriteRequestHttpHandler(
			DefaultMatcher.from( rule ),
			URLMatcher.compile( rule.target() ), next );
	}
}
