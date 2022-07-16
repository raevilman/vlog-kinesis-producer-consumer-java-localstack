package com.therdnotes;

import io.micronaut.runtime.Micronaut;
import software.amazon.awssdk.regions.Region;

public class Application {
    public static void main(String[] args) {
        EnvUtil.getEnvOrError(Constants.KINESIS_STREAM_NAME);
        Region.of(EnvUtil.getEnvOrError(Constants.KINESIS_STREAM_REGION));
        Micronaut.run(Application.class, args);
    }
}
