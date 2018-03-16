package kikaha.commons.url;

import kikaha.commons.Tuple;

public interface URL {

	static String removeTrailingCharacter( final String original ) {
		int offset = original.length() - 1;
		for ( ; original.charAt( offset ) == '/' && offset > 0; offset-- );
		return original.substring(0, offset + 1);
	}

	static Tuple<String, String> fixContentType( String contentType, String defaultEncoding ){
		if ( contentType == null )
			return Tuple.empty();

		final StringCursor cursor = new StringCursor(contentType);
		if ( cursor.shiftCursorToNextChar( ';' ) ) {
			contentType = cursor.substringFromLastMark( 1 );
			cursor.mark(1);
			cursor.end();
			defaultEncoding = cursor.substringFromLastMark();
		}
		return Tuple.of( contentType, defaultEncoding );
	}
}
