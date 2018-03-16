package kikaha.core;

import java.util.*;
import io.undertow.server.*;
import io.undertow.util.*;
import kikaha.commons.url.URLMatcher;
import lombok.*;

@Getter
@Setter
public class SimpleRoutingHandler implements HttpHandler {

	final Map<HttpString, List<Entry>> matchersByMethod = new HashMap<>();

	@NonNull
	volatile HttpHandler fallbackHandler;

	public synchronized void add( final String method, final String url, final HttpHandler handler ) {
		final HttpString methodAsHttpString = new HttpString( method );
		add( methodAsHttpString, url, handler );
	}

	public void add( final HttpString method, final String url, final HttpHandler handler ) {
		List<Entry> list = matchersByMethod.get( method );
		if ( list == null )
			matchersByMethod.put( method, list = new ArrayList<>() );
		list.add( new Entry( url, handler ) );
		Collections.sort( list );
	}

	MatchedEntry retrieveEntryThatMatchesUrlFromList( final List<Entry> list, final String url ) {
		final Map<String, String> matchedPathParameters = new HashMap<>();
		for ( final Entry entry : list )
			if ( entry.getMatcher().matches( url, matchedPathParameters ) )
				return new MatchedEntry( url, entry.getHandler(), matchedPathParameters );
		return null;
	}

	// UNCHECKED: It throws Exception because it implements a superinterface method
	@Override
	public void handleRequest( final HttpServerExchange exchange ) throws Exception {
	// CHECKED
		final HttpString method = exchange.getRequestMethod();
		final List<Entry> list = matchersByMethod.get( method );

		if ( list != null ) {
			final String relativePath = exchange.getRelativePath();
			final MatchedEntry entry = retrieveEntryThatMatchesUrlFromList( list, relativePath );
			if ( entry != null ) {
				handleRequest( exchange, entry );
				return;
			}
		}

		fallbackHandler.handleRequest( exchange );
	}

	void handleRequest( final HttpServerExchange exchange, final MatchedEntry entry ) throws Exception {
		final PathTemplateMatch templateMatch = new PathTemplateMatch( entry.getUrl(), entry.getMatchedPathParameters() );
		exchange.putAttachment( PathTemplateMatch.ATTACHMENT_KEY, templateMatch );
		entry.getHandler().handleRequest( exchange );
	}
}

@Value
class Entry implements Comparable<Entry> {

	public Entry( final String url, final HttpHandler handler ) {
		this.handler = handler;
		this.matcher = URLMatcher.compile( url, true );
		this.url = url;
	}

	final String url;
	final URLMatcher matcher;
	final HttpHandler handler;

	@Override
	public int compareTo(Entry entry) {
		return matcher.compareTo( entry.matcher );
	}
}

@Value
class MatchedEntry {

	final String url;
	final HttpHandler handler;
	final Map<String, String> matchedPathParameters;
}