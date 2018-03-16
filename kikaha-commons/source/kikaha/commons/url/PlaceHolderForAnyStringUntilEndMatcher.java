package kikaha.commons.url;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaceHolderForAnyStringUntilEndMatcher implements Matcher {

	final String placeholder;
	final boolean doNotIgnoreSlashes;

	@Override
	public boolean matches( final StringCursor string , final Map<String, String> foundParameters  ) {
		string.mark();
		string.end();
		foundParameters.put(placeholder, string.substringFromLastMark());
		return true;
	}

	@Override
	public void replace( final StringBuilder buffer , final Map<String, String> foundParameters  ) {
		if ( foundParameters.containsKey( placeholder ) )
			buffer.append( foundParameters.get( placeholder ) );
	}

	@Override
	public String toString() {
		return "PlaceHolder( " + placeholder + ": *" + (doNotIgnoreSlashes ? "/" : "") + " )";
	}
}
