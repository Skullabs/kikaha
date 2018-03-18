package kikaha.cloud.aws.lambda.apt;

import static java.lang.String.format;

import kikaha.apt.GenerableClass;
import lombok.*;

/**
 *
 */
@Getter
@RequiredArgsConstructor
public class AmazonLambdaMethodData implements GenerableClass {

	final String typeName;
	final String packageName;
	final String methodName;
	final String methodParams;
	final String returnType;
	final String httpPath;
	final String httpMethod;

	@Getter( lazy = true )
	private final long identifier = createIdentifier();

	@Override
	public String toString() {
		return format( "%-70s -> %s:%s ",
				getType() + "." + getMethodName(), getHttpMethod(), getHttpPath() );
	}
}
