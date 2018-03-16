package kikaha.core.modules.smart;

import java.util.Set;
import kikaha.commons.url.URLMatcher;
import lombok.AllArgsConstructor;

/**
 * Configurations for CORS requests.
 */
@AllArgsConstructor
public class CORSConfig {

	boolean alwaysAllowOrigin;
	boolean allowCredentials;
	Set<String> allowedMethods;
	Set<String> allowedOrigins;
	Set<URLMatcher> allowedOriginMatchers;

	public String toString(){
		return "alwaysAllowOrigin: " + alwaysAllowOrigin + "; "
			+  "allowCredentials: " + allowCredentials + "; "
			+  "allowedMethods: " + allowedMethods + "; "
			+  "allowedOrigins: " + allowedOrigins;
	}
}
