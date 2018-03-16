package kikaha.commons.url;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class EndsWithMatcher implements Matcher {

	final char[] patternChars;

	@Override
	public boolean matches( final StringCursor string , final Map<String, String> foundParameters  ) {
		val pattern = new StringCursor( patternChars );
		final char firstPatternChar = pattern.next();
		return string.shiftCursorToNextChar( firstPatternChar )
				&& pattern.matches( string );
	}

	@Override
	public void replace( final StringBuilder buffer, final Map<String, String> foundParameters ) {
		buffer.append( patternChars );
	}
}
