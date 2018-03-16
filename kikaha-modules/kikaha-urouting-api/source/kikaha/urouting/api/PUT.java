package kikaha.urouting.api;

import java.lang.annotation.*;

@Inherited
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
public @interface PUT {
}
