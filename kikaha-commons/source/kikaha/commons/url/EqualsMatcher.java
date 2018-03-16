package kikaha.commons.url;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class EqualsMatcher implements Matcher {

	final char[] patternChars;

	public EqualsMatcher( final String pattern ) {
		patternChars = pattern.toCharArray();
	}

	@Override
	public boolean matches( final StringCursor string , final Map<String, String> foundParameters  ) {
		val pattern = new StringCursor( patternChars );
		return pattern.matches( string );
	}

	@Override
	public void replace( final StringBuilder buffer , final Map<String, String> foundParameters  ) {
		buffer.append( patternChars );
	}

	@Override
	public String toString() {
		return "Equals(" + new String(patternChars) + ")";
	}
}
