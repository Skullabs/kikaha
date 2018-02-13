package kikaha.core.modules.smart;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import kikaha.core.url.URLMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@RequiredArgsConstructor @Slf4j
public class ReverseProxyClient extends LoadBalancingProxyClient {

    final RequestMatcher requestMatcher;
    final URLMatcher matcher;

    @Override
    public ProxyTarget findTarget(HttpServerExchange exchange) {
        try {
            val values = new HashMap<String, String>();
            if ( requestMatcher.apply( exchange, values ) ) {
                val uri = new URI( matcher.replace( values ) );
                fixURL( exchange, uri );
                return super.findTarget(exchange);
            }
            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void fixURL( final HttpServerExchange exchange, final URI uri ) {
        val relativePath = uri.getPath() == null ? "/" : uri.getPath();
        exchange.setRelativePath( relativePath );
        exchange.setRequestPath( relativePath );
        exchange.setResolvedPath( relativePath );
        exchange.setRequestURI( uri.toString() );
    }
}
