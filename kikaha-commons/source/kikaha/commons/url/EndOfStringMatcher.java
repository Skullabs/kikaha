package kikaha.commons.url;

import java.util.Map;

public class EndOfStringMatcher implements Matcher {

	@Override
	public boolean matches( final StringCursor string , final Map<String, String> foundParameters  ) {
		return !string.hasNext();
	}

	@Override
	public void replace( final StringBuilder buffer , final Map<String, String> foundParameters  ) {
	}

	@Override
	public String toString() {
		return "End";
	}
}
