package kikaha.core.modules.smart;

import java.util.*;
import io.undertow.server.HttpServerExchange;
import kikaha.core.cdi.helpers.TinyList;
import lombok.*;

import static java.lang.String.join;
import static kikaha.commons.Lang.convert;

@RequiredArgsConstructor
public class DefaultMatcher implements RequestMatcher {

	final List<RequestMatcher> listOfMatchers;

	@Override
	public Boolean apply( final HttpServerExchange t, final Map<String, String> u )
	{
		for ( val matcher : listOfMatchers )
			if ( !matcher.apply( t, u ) )
				return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + join( ",", convert( listOfMatchers, m -> m.toString() ) ) + "]";
	}

	public static RequestMatcher from(final SmartRouteRule rule )
	{
		final TinyList<RequestMatcher> list = new TinyList<>();
		list.add( VirtualHostMatcher.from( rule.virtualHost() ) );
		list.add( PathMatcher.from( rule.path() ) );
		return new DefaultMatcher( list );
	}
}
