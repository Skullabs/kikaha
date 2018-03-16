package kikaha.cloud.aws.alb;

import static kikaha.commons.Lang.convert;
import java.io.IOException;
import java.util.List;
import javax.inject.*;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.model.*;
import kikaha.cloud.aws.iam.AmazonConfigurationProducer.AmazonWebServiceConfiguration;
import kikaha.cloud.smart.ServiceRegistry;
import kikaha.core.cdi.helpers.TinyList;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Singleton
public class AmazonELBServiceRegistry implements ServiceRegistry {

	@Inject AmazonEC2 ec2;
	@Inject AmazonElasticLoadBalancing elb;

	@Inject @Named( "elb" )
	AmazonWebServiceConfiguration amazonWebServiceConfiguration;

	@Override
	public void registerIntoCluster(ApplicationData applicationData) throws IOException {
		final String elbName = amazonWebServiceConfiguration.getString("target-group");
		if ( elbName == null )
			throw new IOException( "Could not automatically join to the AWS Load Balancer named 'null'" );

		log.info( "Registering " + applicationData.getMachineId() + " to AWS Load Balancer " + elbName );
		final RegisterTargetsRequest request = new RegisterTargetsRequest()
				.withTargetGroupArn(elbName)
				.withTargets(createTargetDescription(applicationData));

		final RegisterTargetsResult result = elb.registerTargets(request);
		logResult( "Registering into Load Balancer " + elbName, result.getSdkResponseMetadata() );
		if (result.getSdkHttpMetadata().getHttpStatusCode() != 200)
			throw new IOException("Could not automatically join to AWS Load Balancer named '"+elbName+"'");
	}

	@Override
	public void deregisterFromCluster( ApplicationData applicationData ) throws IOException {
		final String elbName = amazonWebServiceConfiguration.getString("target-group");
		if ( elbName == null )
			throw new IOException( "Could not automatically join to the AWS Load Balancer named 'null'" );

		deregisterFromCluster( applicationData, elbName );
	}

	private void deregisterFromCluster( ApplicationData applicationData, String elbName ) throws IOException {
		final DeregisterTargetsRequest request = new DeregisterTargetsRequest()
				.withTargetGroupArn( elbName )
				.withTargets( createTargetDescription( applicationData ) );

		final DeregisterTargetsResult result = elb.deregisterTargets(request);
		logResult( "Deregistering from Load Balancer " + elbName, result.getSdkResponseMetadata() );
		if (result.getSdkHttpMetadata().getHttpStatusCode() != 200)
			throw new IOException("Could not leave the AWS Load Balancer named '"+elbName+"'");
	}

	private TargetDescription createTargetDescription( final ApplicationData applicationData ) throws IOException {
		return new TargetDescription()
			.withId( applicationData.getMachineId() )
			.withPort( applicationData.getLocalPort() );
	}

	@Override
	public List<ApplicationData> locateSiblingNodesOnTheCluster(ApplicationData applicationData) throws IOException {
		final String elbName = amazonWebServiceConfiguration.getString("target-group");
		if ( elbName == null )
			throw new IOException( "Cannot locate sibling nodes on cluster: no target group defined." );

		final List<ApplicationData> result = new TinyList<>();
		final DescribeTargetHealthResult targetHealthResult = elb.describeTargetHealth(new DescribeTargetHealthRequest().withTargetGroupArn(elbName));
		final List<String> instanceIds = convert( targetHealthResult.getTargetHealthDescriptions(), d-> d.getTarget().getId() );
		final DescribeInstancesResult instancesResult = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceIds));

		instancesResult.getReservations().forEach( r ->
			result.addAll(
				convert( r.getInstances(), i -> convertInstanceToApplicationData( applicationData, i ) )
			)
		);

		return result;
	}

	private ApplicationData convertInstanceToApplicationData(ApplicationData applicationData, Instance instance) {
		return ApplicationData.nodeOfSameApplication(
			applicationData, instance.getInstanceId(), instance.getPrivateIpAddress()
		);
	}

	private void logResult( final String event, final ResponseMetadata sdkResponseMetadata ) {
		log.debug( event + ". Request id: " + sdkResponseMetadata.getRequestId() + ": " + sdkResponseMetadata.toString() );
	}

}
