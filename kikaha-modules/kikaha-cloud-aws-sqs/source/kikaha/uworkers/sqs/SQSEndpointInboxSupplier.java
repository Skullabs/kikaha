package kikaha.uworkers.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import kikaha.uworkers.core.EndpointInbox;
import kikaha.uworkers.core.WorkerEndpointMessageListener;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@RequiredArgsConstructor
public class SQSEndpointInboxSupplier implements EndpointInbox {

	final ObjectMapper mapper;
	final AmazonSQS sqs;
	final String queueUrl;
	final int waitTimeInSeconds, maxNumberOfMessages;

    @Override
    public void receiveMessages(WorkerEndpointMessageListener listener) throws Exception {
        final ReceiveMessageRequest request = new ReceiveMessageRequest( queueUrl )
                .withMaxNumberOfMessages( maxNumberOfMessages )
                .withWaitTimeSeconds(waitTimeInSeconds);
        final ReceiveMessageResult result = sqs.receiveMessage(request);
        for ( Message message : result.getMessages() )
            notifyListener( listener, new SQSExchange( mapper, sqs, queueUrl, message ) );
    }
}
