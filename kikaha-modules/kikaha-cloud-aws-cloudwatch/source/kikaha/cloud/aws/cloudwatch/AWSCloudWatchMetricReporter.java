package kikaha.cloud.aws.cloudwatch;

import static kikaha.commons.Lang.convert;
import java.math.*;
import java.util.*;
import java.util.Map.Entry;
import javax.inject.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import kikaha.cloud.aws.iam.AmazonConfigurationProducer.AmazonWebServiceConfiguration;
import kikaha.cloud.metrics.MetricStore;
import kikaha.cloud.smart.ServiceRegistry.ApplicationData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Singleton
public class AWSCloudWatchMetricReporter implements MetricStore {

	@Inject
	@Named( "cloudwatch" )
	AmazonWebServiceConfiguration configuration;

	@Inject AmazonCloudWatch cloudWatch;
	@Inject ApplicationData applicationData;

	@Getter(lazy = true)
	private final String machineId = retrieveMachineId();

	private String retrieveMachineId() {
		try {
			return String.valueOf(applicationData.getMachineId());
		} catch( Exception cause) {
			log.error( "Can't retrieve the Machine ID", cause );
			throw new UnsupportedOperationException( cause );
		}
	}

	@Override
	public void reportGauges(SortedMap<String, Gauge> gauges) {
		final Set<Entry<String, Gauge>> entries = gauges.entrySet();
		final List<MetricDatum> metrics = convert(entries, entry -> {
			final String format = format(entry.getValue());
			return createMetricEntry(entry.getKey(), Double.valueOf(format));
		});
		reportToCloudWatch( metrics );
	}

	private String format(Gauge gauge) {
		Object o = gauge.getValue();
		if (o instanceof Float) {
			return format(((Float) o).doubleValue());
		} else if (o instanceof Double) {
			return format(((Double) o).doubleValue());
		} else if (o instanceof Byte) {
			return format(((Byte) o).longValue());
		} else if (o instanceof Short) {
			return format(((Short) o).longValue());
		} else if (o instanceof Integer) {
			return format(((Integer) o).longValue());
		} else if (o instanceof Long) {
			return format(((Long) o).longValue());
		} else if (o instanceof BigInteger) {
			return format(((BigInteger) o).doubleValue());
		} else if (o instanceof BigDecimal) {
			return format(((BigDecimal) o).doubleValue());
		} else if (o instanceof Boolean) {
			return format(((Boolean) o) ? 1 : 0);
		}
		return null;
	}

	private String format(long n) {
		return Long.toString(n);
	}

	protected String format(double v) {
		return String.format(Locale.US, "%2.2f", v);
	}

	@Override
	public void reportCounters(SortedMap<String, Counter> counters) {
		final Set<Entry<String, Counter>> entries = counters.entrySet();
		final List<MetricDatum> metrics = convert(entries, entry -> createMetricEntry(entry.getKey(), entry.getValue().getCount()));
		reportToCloudWatch( metrics );
	}

	@Override
	public void reportHistograms(SortedMap<String, Histogram> histogram) {
		final Set<Entry<String, Histogram>> entries = histogram.entrySet();
		final List<MetricDatum> metrics = convert(entries, entry -> createMetricEntry(entry.getKey(), entry.getValue().getCount()));
		reportToCloudWatch( metrics );
	}

	@Override
	public void reportMeters(SortedMap<String, Meter> meters) {
		final Set<Entry<String, Meter>> entries = meters.entrySet();
		final List<MetricDatum> metrics = convert( entries, e -> createMetricEntry(e.getKey(), e.getValue().getOneMinuteRate() ) );
		reportToCloudWatch( metrics );
	}

	@Override
	public void reportTimers(SortedMap<String, Timer> timers) {
		final Set<Entry<String, Timer>> entries = timers.entrySet();
		final List<MetricDatum> metrics = convert(entries, e -> createMetricEntry(e.getKey(), e.getValue().getOneMinuteRate()));
		reportToCloudWatch( metrics );
	}

	private MetricDatum createMetricEntry(String key, double value) {
		log.debug( "Reporting " + key );
		return new MetricDatum()
			.withMetricName(key)
			.withTimestamp(new Date())
			.withValue(value)
			.withDimensions( createDimension(
				"Instance-ID", getMachineId() ) );
	}

	private Dimension createDimension( String key, String value ){
		return new Dimension()
				.withName(key)
				.withValue(value);
	}

	private void reportToCloudWatch( List<MetricDatum> data ) {
		final Iterable<List<MetricDatum>> partitionList = partition(data, 20);
		sendDataToCloudWatch( partitionList );
	}

	private Iterable<List<MetricDatum>> partition(List<MetricDatum> data, int size) {
		final List<List<MetricDatum>> newList = new ArrayList<>();
		final Iterator<MetricDatum> iterator = data.iterator();
		List<MetricDatum> current;

		while ( iterator.hasNext() ) {
			newList.add( current = new ArrayList<>() );
			for (int i = 0; i < size && iterator.hasNext(); i++)
				current.add( iterator.next() );
		}

		return newList;
	}

	private void sendDataToCloudWatch(Iterable<List<MetricDatum>> partitionedData){
		final String namespace = configuration.getString("reporter-namespace");
		log.debug( "Sending data to CloudWatch(region=" + configuration.getRegion().toString() + ", namespace="+namespace+")" );
		for (List<MetricDatum> dataSubset : partitionedData) {
			try {
				PutMetricDataRequest request = new PutMetricDataRequest()
						.withNamespace(namespace)
						.withMetricData(dataSubset);
				log.debug("PutMetricData: " + request);
				final PutMetricDataResult result = cloudWatch.putMetricData(request);
				log.debug("Cloud Watch Result: " + result.getSdkResponseMetadata());
			} catch (Throwable cause) {
				log.error( "Failed to send metrics", cause );
			}
		}
	}
}
