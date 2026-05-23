package com.xxxx.ddd.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Cấu hình ObjectMapper hỗ trợ Java 8 date/time (LocalDateTime, etc.)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Sử dụng StringRedisSerializer để tuần tự hóa và giải tuần tự hóa các giá trị khóa redis
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        // Khóa Hash cũng sử dụng phương thức tuần tự hóa StringRedisSerializer.
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}

//
//package com.xxxx.ddd.infrastructure.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(connectionFactory);
//
//        // Cấu hình serializer cho giá trị và khóa
//        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
//
//        // Sử dụng StringRedisSerializer để tuần tự hóa và giải tuần tự hóa các khóa Redis
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(serializer);
//
//        // Cấu hình cho các khóa hash trong Redis
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashValueSerializer(serializer);
//
//        // Xử lý kết nối thủ công
//        RedisConnection connection = null;
//        try {
//            // Lấy kết nối Redis từ RedisConnectionFactory
//            connection = connectionFactory.getConnection();
//            System.out.println("Kết nối Redis thành công!");
//            // Sử dụng kết nối để thực hiện các thao tác Redis
//            // Ví dụ, có thể ping Redis để kiểm tra kết nối
//            if (connection != null) {
//                System.out.println("Kết nối Redis thành công!");
//            }
//
//        } catch (Exception e) {
//            // Xử lý ngoại lệ nếu có
//            System.err.println("Lỗi khi kết nối Redis: " + e.getMessage());
//        } finally {
//            // Đảm bảo đóng kết nối Redis sau khi sử dụng
//            if (connection != null) {
////                connection.close();
//                System.out.println("Đã đóng kết nối Redis.");
//            }
//        }
//
//        return redisTemplate;
//    }
//}
