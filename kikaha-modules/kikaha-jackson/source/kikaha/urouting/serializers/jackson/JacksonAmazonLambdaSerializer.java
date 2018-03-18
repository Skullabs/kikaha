package kikaha.urouting.serializers.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import kikaha.cloud.aws.lambda.AmazonContentTypeHandler;
import kikaha.cloud.aws.lambda.AmazonRequestException;
import kikaha.urouting.api.Mimes;

import java.io.IOException;
import javax.inject.*;

@Singleton
public class JacksonAmazonLambdaSerializer implements AmazonContentTypeHandler {

    @Inject Jackson jackson;

    @Override
    public String serialize(Object body) throws AmazonRequestException {
        try {
            return jackson.objectMapper().writeValueAsString( body );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T unserialize(String body, Class<T> clazz) throws AmazonRequestException {
        try {
            return jackson.objectMapper().readValue( body, clazz );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String contentType() {
        return Mimes.JSON;
    }
}
