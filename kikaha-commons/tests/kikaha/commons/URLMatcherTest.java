package kikaha.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import kikaha.commons.url.URLMatcher;
import lombok.val;

import org.junit.Test;

public class URLMatcherTest {

	static final int MANY_TIMES = 1000000;

	@Test
	public void ensureThatNoWildcardPatternCanMatchExpectedURLs() {
		final URLMatcher matcher = URLMatcher.compile( "/panel/admin/" );
		assertTrue( matcher.matches( "/panel/admin/", null ) );
		assertFalse( matcher.matches( "/panel/admin/123", null ) );
	}

	@Test
	public void ensureThatOneWildcardPatternCanMatchExpectedURLs() {
		final URLMatcher matcher = URLMatcher.compile( "/panel/*/" );
		assertTrue( matcher.matches( "/panel/admin/", null ) );
		assertFalse( matcher.matches( "/panel/admin/123", null ) );
		assertFalse( matcher.matches( "/admin/panel/", null ) );
	}

	@Test
	public void ensureThatOneWildcardWithNoEnddingSlashPatternCanMatchExpectedURLs() {
		final URLMatcher matcher = URLMatcher.compile( "/panel/*" );
		assertTrue( matcher.matches( "/panel/admin/", null ) );
		assertTrue( matcher.matches( "/panel/admin/123", null ) );
		assertFalse( matcher.matches( "/admin/panel/", null ) );
	}

	@Test
	public void ensureThatTwoWildcardWithNoEnddingSlashPatternCanMatchExpectedURLs() {
		final URLMatcher matcher = URLMatcher.compile( "/panel/*/page/*/" );
		assertFalse( matcher.matches( "/panel/admin/", null ) );
		assertFalse( matcher.matches( "/panel/admin/123", null ) );
		assertFalse( matcher.matches( "/admin/panel/", null ) );
		assertTrue( matcher.matches( "/panel/admin/page//", null ) );
		assertFalse( matcher.matches( "/panel/admin/page/", null ) );
		assertTrue( matcher.matches( "/panel/admin/page/123/", null ) );
	}

	@Test
	public void ensureThatCouldParsePlaceHolders() {
		final Map<String, String> matchedValues = new HashMap<>();
		final URLMatcher matcher = URLMatcher.compile( "/users/{id}/page/*/" );
		assertTrue( matcher.matches( "/users/123/page/10/", matchedValues ) );
		assertEquals( "123", matchedValues.get( "id" ) );
	}

	@Test( timeout = 2000 )
	public void ensureThatCouldApplyStressTestOnUrlMatchingInAffordableTime() {
		final URLMatcher matcher = URLMatcher.compile( "/panel/*/page/*/" );
		for ( int i = 0; i < MANY_TIMES; i++ ) {
			assertFalse( matcher.matches( "/panel/admin/page/", null ) );
			assertTrue( matcher.matches( "/panel/admin/page/123/", null ) );
		}
	}

	@Test
	public void ensureThatCanReplacePatternPlaceholders() {
		val params = new HashMap<String, String>();
		val matcher = URLMatcher.compile( "/users/{id}/page/" );
		assertTrue( matcher.matches( "/users/123/page/", params ) );
		val replacer = URLMatcher.compile( "/new/user/{id}/view/" );
		assertEquals( "/new/user/123/view/", replacer.replace( params ) );
	}

	@Test
	public void ensureThatCanReplacePatternPlaceholdersAtEndOfString() {
		val params = new HashMap<String, String>();
		val matcher = URLMatcher.compile( "/users/{id}" );
		assertTrue( matcher.matches( "/users/123", params ) );
		val replacer = URLMatcher.compile( "/new/user/{id}" );
		assertEquals( "/new/user/123", replacer.replace( params ) );
	}

	@Test
	public void ensureThatCanReplacePatternPlaceholdersForEntireString() {
		val params = new HashMap<String, String>();
		val matcher = URLMatcher.compile( "{string}" );
		assertTrue( matcher.matches( "users/123/*", params ) );
		val replacer = URLMatcher.compile( "/new/{string}" );
		assertEquals( "/new/users/123/*", replacer.replace( params ) );
	}

	@Test
	public void ensureThatCanMatchUrlsWithPlaceHoldersAndDoNotIgnoreSubPaths() {
		val params = new HashMap<String, String>();
		val matcher = URLMatcher.compile( "users/{id}", true );
		assertTrue( "Did not match regular path", matcher.matches( "users/123", params ) );
		assertTrue( "Did match path ending with slash", matcher.matches( "users/123/", params ) );
	}

	@Test
	public void ensureCanMatchRootPath(){
		val params = new HashMap<String, String>();
		val matcher = URLMatcher.compile( "/*", true );
		assertTrue( "Did not matched /", matcher.matches( "/", params ) );
		assertTrue( "Did not matched /path", matcher.matches( "/path", params ) );
		assertTrue( "Did not matched /path/user", matcher.matches( "/path/user", params ) );
	}
}
