package com.xxxx.ddd.infrastructure.gateway;

import com.xxxx.ddd.application.port.payment.PaymentGatewayPort;
import com.xxxx.ddd.domain.model.entity.Payment;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * VNPAY sandbox gateway adapter.
 * Builds signed payment URLs and verifies callback signatures
 * using externalized config from {@link VnPayProperties}.
 */
@Service
@EnableConfigurationProperties(VnPayProperties.class)
public class VnPayGatewayServiceImpl implements PaymentGatewayPort {

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayProperties properties;

    public VnPayGatewayServiceImpl(VnPayProperties properties) {
        this.properties = properties;
    }

    /**
     * Build a signed VNPAY sandbox payment URL for the given payment record.
     */
    @Override
    public String createPaymentUrl(Payment payment) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", properties.tmnCode());
        params.put("vnp_Amount", payment.getAmount()
                .multiply(new java.math.BigDecimal(100))
                .toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_TxnRef", payment.getPaymentId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_OrderInfo", "Payment for order " + payment.getOrderId());
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", properties.returnUrl());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", VNP_DATE_FORMAT.format(LocalDateTime.now()));

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII)
                        .replace("+", "%20");

                hashData.append(encodedName).append('=').append(encodedValue).append('&');
                query.append(encodedName).append('=').append(encodedValue).append('&');
            }
        }

        // Remove trailing '&'
        String hashDataStr = hashData.substring(0, hashData.length() - 1);
        String queryStr = query.substring(0, query.length() - 1);

        String secureHash = hmacSHA512(properties.secretKey(), hashDataStr);
        return properties.payUrl() + "?" + queryStr + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Verify the VNPAY callback signature.
     * The caller must pass all query parameters from the callback URL.
     */
    @Override
    public boolean verifyCallback(Map<String, String> callbackParams) {
        String receivedHash = callbackParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        // Build hash data from sorted params excluding vnp_SecureHash and vnp_SecureHashType
        Map<String, String> sorted = new TreeMap<>(callbackParams);
        sorted.remove("vnp_SecureHash");
        sorted.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String fieldValue = entry.getValue();
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedName = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII)
                        .replace("+", "%20");
                hashData.append(encodedName).append('=').append(encodedValue).append('&');
            }
        }

        if (hashData.isEmpty()) {
            return false;
        }

        String hashDataStr = hashData.substring(0, hashData.length() - 1);
        String expectedHash = hmacSHA512(properties.secretKey(), hashDataStr);
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hashBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPAY signature", e);
        }
    }
}
