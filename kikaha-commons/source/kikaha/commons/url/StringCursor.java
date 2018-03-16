package kikaha.commons.url;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringCursor {

	final char[] chars;
	volatile int cursor;
	volatile int mark;

	public StringCursor( final String chars ) {
		this( chars.toCharArray() );
	}

	public boolean hasNext() {
		return cursor < chars.length;
	}

	public void end() {
		cursor = chars.length;
	}

	public char next() {
		return chars[cursor++];
	}

	public char prev()
	{
		return chars[cursor--];
	}

	/**
	 * Create a mark positioned at current cursor.
	 */
	public void mark() {
		mark = cursor;
	}

	/**
	 * Create a mark positioned at the end of string.
	 */
	public void markAtEnd() {
		mark = cursor;
	}

	/**
	 * Create a mark positioned at current cursor. The {@code shiftedCharacters}
	 * argument fixes the gap between the current cursor and the expected
	 * position relative to cursor.
	 *
	 */
	public void mark( final int shiftedCharacters ) {
		mark = cursor + shiftedCharacters;
	}

	public void cursorAt( final int newPos ){
		cursor = newPos;
	}

	public void reset() {
		cursor = 0;
	}

	/**
	 * Reset cursor to same point defined by last mark.
	 */
	public void flip() {
		cursor = mark;
	}

	/**
	 * Check if the following characters in cursor matches the remaining
	 * characters in {@code string} argument.
	 *
	 * @param string
	 * @return
	 */
	boolean matches( final StringCursor string ) {
		while ( hasNext() && string.hasNext() )
			if ( next() != string.next() )
				return false;
		return !hasNext();
	}

	/**
	 * Shift the cursor to the next occurrence of {@code ch} and returns
	 * {@code true}. Otherwise, it will keep the cursor at same position, but
	 * will return {@code false}.
	 *
	 * @param ch
	 * @return
	 */
	public boolean shiftCursorToNextChar( final char ch ) {
		mark();
		while ( hasNext() )
			if ( next() == ch )
				return true;
		flip();
		return false;
	}

	public String substringUntilCursor() {
		return new String( chars, 0, cursor );
	}

	/**
	 * Return a new string from last mark until current cursor position.
	 *
	 * @return
	 */
	public String substringFromLastMark() {
		return substringFromLastMark( 0 );
	}

	/**
	 * Return a new string from last mark until current cursor position. The
	 * {@code ignoredLastChars} argument defines which amount of characters
	 * before the current cursor should be ignored.
	 *
	 * @return
	 */
	public String substringFromLastMark( final int ignoredLastChars ) {
		final StringBuilder buffer = new StringBuilder();
		for ( int i = mark; i < cursor - ignoredLastChars; i++ )
			buffer.append( chars[i] );
		return buffer.toString();
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		for ( int i = 0; i < cursor; i++ )
			buffer.append( chars[i] );
		if ( cursor < chars.length ) {
			buffer.append('[').append(chars[cursor]).append(']');
			for (int i = cursor + 1; i < chars.length; i++)
				buffer.append(chars[i]);
		} else
			buffer.append("[]");
		return buffer.toString();
	}
}
