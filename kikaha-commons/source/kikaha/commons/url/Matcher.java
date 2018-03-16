package kikaha.commons.url;

import java.util.Map;

public interface Matcher {

	boolean matches( final StringCursor string, final Map<String, String> foundParameters );

	void replace( final StringBuilder buffer , final Map<String, String> foundParameters  );
}
