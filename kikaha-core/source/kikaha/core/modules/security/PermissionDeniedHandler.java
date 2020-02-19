package kikaha.core.modules.security;

import io.undertow.server.HttpServerExchange;

public interface PermissionDeniedHandler {

    void handle(final HttpServerExchange exchange);
}
