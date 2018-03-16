package kikaha.urouting.api.converter;


import kikaha.urouting.api.RoutingException;

public class ConversionException extends RoutingException {

	private static final long serialVersionUID = 2695056089411684745L;

	public ConversionException( String message, Throwable cause ) {
		super( message, cause );
	}

	public ConversionException( String message ) {
		super( message );
	}

	public ConversionException( Throwable cause ) {
		super( cause );
	}
}
