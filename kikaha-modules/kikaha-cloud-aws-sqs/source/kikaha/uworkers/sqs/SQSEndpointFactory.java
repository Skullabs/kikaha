package kikaha.uworkers.sqs;

import javax.inject.*;
import com.amazonaws.jmespath.ObjectMapperSingleton;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import kikaha.config.Config;
import kikaha.uworkers.api.WorkerRef;
import kikaha.uworkers.core.*;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Singleton
public class SQSEndpointFactory implements PollingEndpointFactory {

	@Inject AmazonSQS amazonSQS;

	@Override
	public EndpointInbox createSupplier( EndpointConfig endpoint ) {
		final Config endpointConfig = endpoint.getConfig();
		final ObjectMapper objectMapper = ObjectMapperSingleton.getObjectMapper();
		return new SQSEndpointInboxSupplier( objectMapper, amazonSQS,
			queueUrl(endpointConfig),
			endpointConfig.getInteger( "wait-time-seconds", 20 ),
            endpointConfig.getInteger( "max-number-of-messages", 10 ));
	}

	@Override
	public WorkerRef createWorkerRef( EndpointConfig endpoint ) {
		final Config endpointConfig = endpoint.getConfig();
		final ObjectMapper objectMapper = ObjectMapperSingleton.getObjectMapper();
		return new SQSWorkerRef(objectMapper, amazonSQS, queueUrl(endpointConfig));
	}

	private String queueUrl( Config endpointConfig ){
		String url = endpointConfig.getString( "url" );
		if ( url != null )
			return url;
		String queueName = endpointConfig.getString( "queue-name" );
		if ( queueName == null )
			throw new RuntimeException( "Configuration 'url' or 'queue-name' is required in SQS worker" );
		return createQueueAndGetUrl( queueName );
	}

	private String createQueueAndGetUrl(String queueName) {
		try {
			final CreateQueueRequest createQueueRequest = new CreateQueueRequest( queueName );
			return amazonSQS.createQueue(createQueueRequest).getQueueUrl();
		} catch ( Exception e ){
			log.error( "Failed to create queue with name " + queueName + " in Amazon SQS" );
			throw new RuntimeException( e );
		}
	}

}
