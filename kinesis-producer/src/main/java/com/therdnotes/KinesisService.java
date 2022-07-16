package com.therdnotes;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.therdnotes.tweet.Tweet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.utils.StringUtils;

import java.nio.ByteBuffer;

import static com.therdnotes.Constants.KINESIS_CUSTOM_ENDPOINT_HOST;
import static com.therdnotes.Constants.KINESIS_CUSTOM_ENDPOINT_PORT;

@Singleton
public class KinesisService {

    @Inject
    ObjectMapper objectMapper;

    final KinesisProducer kinesisProducer;

    final String STREAM_NAME;

    public KinesisService() {
        STREAM_NAME = EnvUtil.getEnvOrError(Constants.KINESIS_STREAM_NAME);
        String region = EnvUtil.getEnvOrError(Constants.KINESIS_STREAM_REGION);
        KinesisProducerConfiguration producerConfiguration = new KinesisProducerConfiguration();
        producerConfiguration.setRegion(region);

        String kinesisHost = EnvUtil.getEnvOrDefault(KINESIS_CUSTOM_ENDPOINT_HOST, "");
        if (!StringUtils.isBlank(kinesisHost)) {
            int kinesisPort = Integer.parseInt(EnvUtil.getEnvOrDefault(KINESIS_CUSTOM_ENDPOINT_PORT, "4566"));
            producerConfiguration.setKinesisEndpoint(kinesisHost);
            producerConfiguration.setKinesisPort(kinesisPort);
            producerConfiguration.setCloudwatchEndpoint(kinesisHost);
            producerConfiguration.setCloudwatchPort(kinesisPort);
            producerConfiguration.setVerifyCertificate(false);
        }
        kinesisProducer = new KinesisProducer(producerConfiguration);
    }

    public void addRecord(Tweet tweet) throws JsonProcessingException {
        String partitionKey = String.valueOf(System.currentTimeMillis() / 1000);
        ByteBuffer data = ByteBuffer.wrap(objectMapper.writeValueAsBytes(tweet));
        kinesisProducer.addUserRecord(STREAM_NAME, partitionKey, data);
    }
}
