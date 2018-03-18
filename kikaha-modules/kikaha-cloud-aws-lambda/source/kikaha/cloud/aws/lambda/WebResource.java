package kikaha.cloud.aws.lambda;

import java.lang.annotation.*;

/**
 * Basically, this is a copy from {@code kikaha.core.modules.http.WebResource}.
 * This was made due to an effort to make this lambda API as lean as possible.<br>
 * <br>
 * <b>Developers are not encouraged to use this annotation.</b>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebResource {

    String path();

    String method() default "GET";
}

