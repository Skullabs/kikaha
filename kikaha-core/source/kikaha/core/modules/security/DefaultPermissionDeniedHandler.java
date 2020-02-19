package kikaha.core.modules.security;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import lombok.val;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Singleton
public class DefaultPermissionDeniedHandler implements PermissionDeniedHandler {

    @Inject AuthenticationEndpoints authenticationEndpoints;

    @Override
    public void handle(final HttpServerExchange exchange) {
        if (!exchange.isResponseStarted()) {
            if (isPermissionDeniedPageEmpty())
                sendForbiddenError(exchange);
            else
                redirectToPermissionDeniedPage(exchange);
        }
    }

    protected boolean isPermissionDeniedPageEmpty() {
        return authenticationEndpoints.getPermissionDeniedPage() == null || authenticationEndpoints.getPermissionDeniedPage().isEmpty();
    }

    protected void sendForbiddenError(final HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.FORBIDDEN);
        exchange.getResponseSender().send("Permission Denied");
    }

    protected void redirectToPermissionDeniedPage(final HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.SEE_OTHER);
        exchange.getResponseHeaders().put(Headers.LOCATION, permissionDeniedPage(exchange));
    }

    private String permissionDeniedPage(final HttpServerExchange exchange) {
        val currentPage = new StringBuilder(exchange.getRequestURI());
        if (!exchange.getQueryString().isEmpty())
            currentPage.append('?').append(exchange.getQueryString());

        val currentPageEncoded = encode(currentPage.toString());
        return authenticationEndpoints.getPermissionDeniedPage().replace("{current-page}", currentPageEncoded);
    }

    private String encode(final String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new PermissionDeniedException(e);
        }
    }
}
