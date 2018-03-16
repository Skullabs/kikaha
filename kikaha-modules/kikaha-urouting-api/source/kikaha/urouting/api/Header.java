package kikaha.urouting.api;

import java.util.List;

public interface Header {
	String name();
	List<String> values();
	void add( String value );
}
