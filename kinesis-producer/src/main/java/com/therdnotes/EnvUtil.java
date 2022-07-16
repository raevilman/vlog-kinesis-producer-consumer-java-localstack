package com.therdnotes;

import software.amazon.awssdk.utils.StringUtils;

public class EnvUtil {

    public static String getEnvOrError(String varName) {
        String value = System.getenv(varName);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalStateException("Environment variable(" + varName + ") is not provided");
        } else {
            return value;
        }
    }

    public static String getEnvOrDefault(String varName, String defaultValue) {
        String value = System.getenv(varName);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }
}