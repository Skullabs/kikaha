package kikaha.core.modules.smart;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.protocols.ssl.UndertowXnioSsl;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import kikaha.config.Config;
import kikaha.core.DeploymentContext;
import kikaha.core.modules.Module;
import kikaha.core.url.URLMatcher;
import lombok.Getter;
import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.xnio.OptionMap;
import org.xnio.Xnio;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Singleton
public class RewriteRoutesModule implements Module {

	@Inject Config config;
	List<SmartRouteRule> rewriteRoutes;
	List<SmartRouteRule> reverseRoutes;
	boolean isHttp2EnabledForProxy;

	@PostConstruct
	public void loadConfig(){
		List<Config> configs = config.getConfigList("server.smart-routes.rewrite");
		rewriteRoutes = configs.stream().map( c-> SmartRouteRule.from(c) ).collect(Collectors.toList());
		configs = config.getConfigList("server.smart-routes.reverse");
		reverseRoutes = configs.stream().map( c-> SmartRouteRule.from(c) ).collect(Collectors.toList());
        isHttp2EnabledForProxy = config.getBoolean( "server.smart-routes.reverse-with-http2", true );
	}

	public void load(final Undertow.Builder server, final DeploymentContext context )
	{
	    try {
            deployRewriteRoutes(context);
            deployReverseProxyRoutes(context);
        } catch ( Throwable cause ) {
	        throw new RuntimeException( "Failed to load 'Smart Routes' module", cause );
        }
	}

	private void deployRewriteRoutes( final DeploymentContext context )
	{
	    if ( !rewriteRoutes.isEmpty() )
            log.info( "Rewrite rules:" );

		for ( SmartRouteRule route : rewriteRoutes ) {
			log.info( "  > " + route );
			final HttpHandler rewriteHandler = RewriteRequestHttpHandler.from( route, context.rootHandler() );
			context.rootHandler( rewriteHandler );
		}
	}

    private void deployReverseProxyRoutes(final DeploymentContext context) throws URISyntaxException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        var lastHandler = context.rootHandler();
        val ssl = new UndertowXnioSsl( Xnio.getInstance(), OptionMap.EMPTY );
        val options = isHttp2EnabledForProxy
            ? OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)
            : OptionMap.EMPTY ;

        if ( !reverseRoutes.isEmpty() )
            log.info( "Reverse Proxy rules:" );

        for ( val rule : reverseRoutes ) {
            log.info( "  > " + rule );
            val target = URLMatcher.compile( rule.target() );
            val proxyClient = createClientFor( rule, target, ssl, options );
            lastHandler = new ProxyHandler( proxyClient, lastHandler );
        }
        context.rootHandler( lastHandler );
    }

    private ProxyClient createClientFor(SmartRouteRule rule, URLMatcher target, UndertowXnioSsl ssl, OptionMap options) throws URISyntaxException {
        val proxyClient = new ReverseProxyClient( DefaultMatcher.from( rule ), target );
        val uri = asHost(target);
        if ( "https".equals( uri.getScheme() ) ) {
            proxyClient.addHost(uri, null, ssl, options );
        } else
            proxyClient.addHost(uri);
        return proxyClient.setProblemServerRetry( 60 );
    }

    private URI asHost(URLMatcher target ) throws URISyntaxException {
        val uri = new URI( target.replace( Collections.emptyMap() ) );
        return new URI( uri.getScheme() + "://" + uri.getAuthority() + "/" );
    }
}