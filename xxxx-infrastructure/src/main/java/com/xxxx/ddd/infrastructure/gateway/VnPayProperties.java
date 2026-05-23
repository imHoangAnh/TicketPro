package com.xxxx.ddd.infrastructure.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vnpay")
public record VnPayProperties(
        String tmnCode,
        String secretKey,
        String payUrl,
        String returnUrl
) {
}
