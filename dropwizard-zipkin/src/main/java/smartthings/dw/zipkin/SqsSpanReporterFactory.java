package smartthings.dw.zipkin;

import com.amazonaws.auth.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.hibernate.validator.constraints.NotEmpty;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.sqs.SQSSender;

import java.util.Optional;

@JsonTypeName("sqs")
public class SqsSpanReporterFactory implements SpanReporterFactory {

    @NotEmpty
    private String queueUrl;

    private String awsAccessKeyId;

    private String awsSecretAccessKey;

    private String awsRoleArn;

    @JsonProperty
    public String getQueueUrl() {
        return queueUrl;
    }

    @JsonProperty
    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    @JsonProperty
    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    @JsonProperty
    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    @JsonProperty
    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    @JsonProperty
    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    @JsonProperty
    public String getAwsRoleArn() {
        return awsRoleArn;
    }

    @JsonProperty
    public void setAwsRoleArn(String awsRoleArn) {
        this.awsRoleArn = awsRoleArn;
    }

    @Override
    public Reporter<Span> build() {
        final AWSCredentialsProvider baseCredsProvider = (awsAccessKeyId != null && awsSecretAccessKey != null)?
            new AWSCredentialsProviderChain(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey))
            ) : new DefaultAWSCredentialsProviderChain();

        Optional<AWSCredentialsProvider> maybeRoleCredsProvider = Optional.ofNullable(awsRoleArn)
            .map(arn -> new STSAssumeRoleSessionCredentialsProvider
                .Builder(awsRoleArn, "zipkin")
                .withStsClient(AWSSecurityTokenServiceClientBuilder
                    .standard()
                    .withCredentials(baseCredsProvider)
                    .build())
                .build());

        SQSSender sender = SQSSender.builder()
            .queueUrl(queueUrl)
            .credentialsProvider(maybeRoleCredsProvider.orElse(baseCredsProvider))
            .build();

        return AsyncReporter.builder(sender).build();
    }
}
