package kikaha.cloud.aws.lambda;

import com.fasterxml.jackson.jr.ob.JSON;
import kikaha.urouting.api.Mimes;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Singleton @Accessors(fluent = true)
@SuppressWarnings("unchecked")
public class JacksonJrAmazonLambdaSerializer implements AmazonContentTypeHandler {

    @Getter
    final String contentType = Mimes.JSON;

    @Override
    public String serialize(Object body) throws AmazonRequestException {
        try {
            return JSON.std.asString( body );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T unserialize(String body, Class<T> clazz) throws AmazonRequestException {
        try {
            if ( Map.class.isAssignableFrom( clazz ) )
                return (T) JSON.std.anyFrom( body );
            return JSON.std.beanFrom( clazz, body );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
