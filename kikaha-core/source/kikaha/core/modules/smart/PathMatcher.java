package kikaha.core.modules.smart;

import io.undertow.server.HttpServerExchange;

import java.util.Map;

import kikaha.commons.url.URLMatcher;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class PathMatcher implements RequestMatcher {

	final URLMatcher path;

	@Override
	public Boolean apply( final HttpServerExchange exchange, final Map<String, String> properties )
	{
		val relativePath = exchange.getRelativePath();
		return path.matches( relativePath, properties );
	}

	@Override
	public String toString() {
		return "path=" + path;
	}

	public static RequestMatcher from(final String path )
	{
		return new PathMatcher( URLMatcher.compile( path ) );
	}
}
