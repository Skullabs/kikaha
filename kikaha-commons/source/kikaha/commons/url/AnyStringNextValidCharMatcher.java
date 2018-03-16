package kikaha.commons.url;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnyStringNextValidCharMatcher implements Matcher {

	final char nextValidChar;

	@Override
	public boolean matches( final StringCursor string , final Map<String, String> foundParameters  ) {
		return string.shiftCursorToNextChar( nextValidChar );
	}

	@Override
	public void replace( final StringBuilder buffer , final Map<String, String> foundParameters  ) {
		throw new UnsupportedOperationException();
	}
}
