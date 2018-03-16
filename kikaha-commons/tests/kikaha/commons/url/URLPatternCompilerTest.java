package kikaha.commons.url;

import lombok.val;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class URLPatternCompilerTest {

	@Test
	public void ensureThatCanCompileASimpleURLPatternIntoOneMatcherRule() {
		val compiler = new URLPatternCompiler( false );
		compiler.compile( "/panel/admin/" );
		assertThatFitsACompiledPatterForExpectedNumberOfWildcards( compiler, 0 );
	}

	@Test
	public void ensureThatCanCompileTheUserEndpointWithAsteriskURLPatternIntoTwoMatcherRules() {
		val compiler = new URLPatternCompiler( false );
		compiler.compile( "/user/*/" );
		assertThatFitsACompiledPatterForExpectedNumberOfWildcards( compiler, 1 );
	}

	@Test
	public void ensureThatCanCompileTheUserCredentialEndpointURLPatternIntoTwoMatcherRules() {
		val compiler = new URLPatternCompiler( false );
		compiler.compile( "/user/*/credential/" );
		assertThatFitsACompiledPatterForExpectedNumberOfWildcards( compiler, 2 );
	}

	@Test
	public void ensureThatCanCompileTheURLPatternThatMatchesAnyUserRequestIntoTwoMatcherRules() {
		val compiler = new URLPatternCompiler( false );
		compiler.compile( "/user/*" );
		assertThatHasExpectedSize( compiler, 3 );
		assertFirstMatcherIsEqualsAndLastIsEndOfString( compiler );
		assertIsInstance( compiler.patternMatchers.get( 1 ), AnyStringUntilEndMatcher.class );
	}

	@Test
	public void ensureThatCanCompileTheURLPatternWithTwoPlaceHoldersIntoThreeMatcherRules() {
		val compiler = new URLPatternCompiler( false );
		compiler.compile( "/user/*/page/*/" );
		assertThatFitsACompiledPatterForExpectedNumberOfWildcards( compiler, 3 );
	}

	private void assertThatFitsACompiledPatterForExpectedNumberOfWildcards( final URLPatternCompiler compiler, final int expectedWildcards ) {
		final int expectedSize = expectedWildcards + 2;
		assertThatHasExpectedSize( compiler, expectedSize );
		assertFirstMatcherIsEqualsAndLastIsEndOfString( compiler );
	}

	private void assertThatHasExpectedSize(final kikaha.commons.url.URLPatternCompiler compiler, final int expectedSize ) {
		assertThat( compiler.patternMatchers.size(), is( expectedSize ) );
	}

	private void assertFirstMatcherIsEqualsAndLastIsEndOfString( final URLPatternCompiler compiler ) {
		final List<Matcher> patternMatchers = compiler.patternMatchers;
		assertIsInstance( patternMatchers.get( 0 ), EqualsMatcher.class );
		assertIsInstance( patternMatchers.get( patternMatchers.size() - 1 ), EndOfStringMatcher.class );
	}

    private static <T> void assertIsInstance(final T object, final Class<? extends T> clazz ) {
        if ( !clazz.isInstance( object ) )
            fail( object + " is not instance of " + clazz.getCanonicalName() );
    }
}
