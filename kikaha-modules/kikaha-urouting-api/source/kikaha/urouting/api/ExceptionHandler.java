package kikaha.urouting.api;

public interface ExceptionHandler<E extends Throwable> {

	Response handle( E exception );
}