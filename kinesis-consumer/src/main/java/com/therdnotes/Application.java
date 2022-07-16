package com.therdnotes;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClientBuilder;
import software.amazon.awssdk.utils.StringUtils;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import java.net.URI;
import java.util.UUID;

import static com.therdnotes.Constants.KINESIS_CUSTOM_ENDPOINT;

public class Application {
    private final String streamName;
    private final Region region;

    public Application() {
        streamName = EnvUtil.getEnvOrError(Constants.KINESIS_STREAM_NAME);
        region = Region.of(EnvUtil.getEnvOrError(Constants.KINESIS_STREAM_REGION));
    }

    public static void main(String[] args) {
        new Application().run();
    }

    public void run() {
        /**
         * Sets up configuration for the KCL, including DynamoDB and CloudWatch dependencies. The final argument, a
         * ShardRecordProcessorFactory, is where the logic for record processing lives, and is located in a private
         * class below.
         */
        KinesisAsyncClientBuilder kinesisAsyncClientBuilder = KinesisAsyncClient.builder().region(this.region);
        DynamoDbAsyncClientBuilder dynamoDbAsyncClientBuilder = DynamoDbAsyncClient.builder().region(region);
        CloudWatchAsyncClientBuilder cloudWatchAsyncClientBuilder = CloudWatchAsyncClient.builder().region(region);

        String customEndpoint = EnvUtil.getEnvOrDefault(KINESIS_CUSTOM_ENDPOINT, "");
        if (!StringUtils.isBlank(customEndpoint)) {
            URI kinesisEndpointURI = URI.create(customEndpoint);
            kinesisAsyncClientBuilder.endpointOverride(kinesisEndpointURI);
            dynamoDbAsyncClientBuilder.endpointOverride(kinesisEndpointURI);
            cloudWatchAsyncClientBuilder.endpointOverride(kinesisEndpointURI);
        }

        KinesisAsyncClient kinesisAsyncClient = KinesisClientUtil.createKinesisAsyncClient(kinesisAsyncClientBuilder);
        DynamoDbAsyncClient dynamoClient = dynamoDbAsyncClientBuilder.build();
        CloudWatchAsyncClient cloudWatchClient = cloudWatchAsyncClientBuilder.build();

        ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, streamName, kinesisAsyncClient, dynamoClient, cloudWatchClient, UUID.randomUUID().toString(), new SampleRecordProcessorFactory());

        /**
         * The Scheduler (also called Worker in earlier versions of the KCL) is the entry point to the KCL. This
         * instance is configured with defaults provided by the ConfigsBuilder.
         */
        Scheduler scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig().retrievalSpecificConfig(new PollingConfig(streamName, kinesisAsyncClient))
        );

        /**
         * Kickoff the Scheduler. Record processing of the stream of dummy data will continue indefinitely
         * until an exit is triggered.
         */
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }
}