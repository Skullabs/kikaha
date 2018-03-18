package kikaha.core.cdi.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Qualifier;

public class DefaultFieldQualifierExtractor implements FieldQualifierExtractor {

	@Override
	public boolean isAnnotatedWithQualifierAnnotation( Class<? extends Annotation> ann ) {
		return ann.isAnnotationPresent( Qualifier.class );
	}

	@Override
	public boolean isASingleElementProvider( Field field ) {
		return field.isAnnotationPresent( Inject.class );
	}

	@Override
	public boolean isAManyElementsProvider( Field field ) {
		return isASingleElementProvider( field )
				&& fieldRepresentsACollection( field )
				&& field.isAnnotationPresent( Typed.class );
	}

	public static boolean fieldRepresentsACollection( final Field field ) {
		final Class<?> type = field.getType();
		return Iterable.class.equals( type ) || Collection.class.equals( type ) || List.class.equals( type );
	}
}
