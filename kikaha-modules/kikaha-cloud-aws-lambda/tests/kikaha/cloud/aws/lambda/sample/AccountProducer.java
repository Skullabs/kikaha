package kikaha.cloud.aws.lambda.sample;

import kikaha.cloud.aws.lambda.AmazonContextProducer;
import kikaha.cloud.aws.lambda.AmazonHttpRequest;
import lombok.*;

import java.util.*;
import javax.inject.*;

@Singleton
public class AccountProducer implements AmazonContextProducer<Account> {

    public static final String HEADER = "X-Username";

    @Override
    public Account produce(AmazonHttpRequest request) {
        return new Account( request.getHeaders().get(HEADER) );
    }
}
