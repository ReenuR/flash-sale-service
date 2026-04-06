package com.flashsale.config;

public class RedisKeys {
    private RedisKeys() {
        // utility class — prevent instantiation
    }
    public static String inventoryKey(String saleId) {
        return "inventory:" + saleId;
    }

    public static String rateLimitKey(String userId) {
        return "rate_limit:" + userId;
    }
}
