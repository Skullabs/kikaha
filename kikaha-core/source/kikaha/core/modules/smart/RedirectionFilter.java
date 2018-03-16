package kikaha.core.modules.smart;

import java.util.*;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.smart.FilterChainFactory.FilterChain;
import kikaha.commons.url.URLMatcher;
import kikaha.core.modules.undertow.Redirect;
import lombok.RequiredArgsConstructor;

/**
 * A {@link Filter} that redirects request to new URLs.
 */
@RequiredArgsConstructor
public class RedirectionFilter implements Filter {

	final RequestMatcher requestMatcher;
	final URLMatcher targetPath;

	@Override
	public void doFilter(HttpServerExchange exchange, FilterChain chain) throws Exception {
		final Map<String, String> properties = new HashMap<>();
		if ( requestMatcher.apply( exchange, properties ) ) {
			final String replaced = targetPath.replace(properties);
			Redirect.to( exchange, replaced );
		} else
			chain.runNext();
	}

	public static Filter from(final SmartRouteRule rule )
	{
		return new RedirectionFilter(
				DefaultMatcher.from( rule ),
				URLMatcher.compile( rule.target() ) );
	}
}
