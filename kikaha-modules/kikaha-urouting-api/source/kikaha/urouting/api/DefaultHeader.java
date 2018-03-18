package kikaha.urouting.api;

import kikaha.core.cdi.helpers.TinyList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter @ToString
@RequiredArgsConstructor
@Accessors(fluent = true)
public class DefaultHeader implements Header {

	final String name;
	final List<String> values;

	public void add( String value ) {
		values.add( value );
	}

	public static Header createHeader( String name, String value ) {
		return new DefaultHeader( name, new TinyList<>( value ) );
	}
}
