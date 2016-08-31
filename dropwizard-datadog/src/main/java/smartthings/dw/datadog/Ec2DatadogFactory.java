package smartthings.dw.datadog;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.rholder.retry.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.dropwizard.metrics.BaseReporterFactory;
import org.coursera.metrics.datadog.AwsHelper;
import org.coursera.metrics.datadog.DatadogReporter;
import org.coursera.metrics.datadog.transport.AbstractTransportFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.coursera.metrics.datadog.DatadogReporter.Expansion.*;

@JsonTypeName("ec2Datadog")
public class Ec2DatadogFactory extends BaseReporterFactory {

	private final Retryer<String> awsRetrier = RetryerBuilder.<String>newBuilder()
			.retryIfException()
			.withStopStrategy(StopStrategies.stopAfterAttempt(4))
			.withBlockStrategy(BlockStrategies.threadSleepStrategy())
			.withWaitStrategy(WaitStrategies.fixedWait(5L, TimeUnit.SECONDS))
			.withRetryListener(new LoggingRetryListener("fetching instanceId", 5))
			.build();

	@JsonProperty
	private List<String> tags = Lists.newArrayList();

	@JsonProperty
	private List<DatadogReporter.Expansion> expansions = Lists.newArrayList(
			RATE_1_MINUTE, MAX, MEAN, P95, P99, P999
	);

	@Valid
	@NotNull
	@JsonProperty
	private AbstractTransportFactory transport = null;

	public ScheduledReporter build(MetricRegistry registry) {
		String host;
		try {
			host = awsRetrier.call(AwsHelper::getEc2InstanceId);
		} catch (ExecutionException | RetryException e) {
			throw new RuntimeException(e);
		}

		return DatadogReporter.forRegistry(registry)
				.withTransport(transport.build())
				.withHost(host)
				.withExpansions(Sets.newEnumSet(expansions, DatadogReporter.Expansion.class))
				.withTags(tags)
				.filter(getFilter())
				.convertDurationsTo(getDurationUnit())
				.convertRatesTo(getRateUnit())
				.build();
	}
}
