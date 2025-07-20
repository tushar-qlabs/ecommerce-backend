package dev.tushar.ecommerceapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        // It's main responsibility is to convert Java objects to and
        // from JSON, but is mainly used to handle POJOs.

        objectMapper.registerModule(new JavaTimeModule());
        // We're now registering JavaTimeModule to our ObjectMapper
        // which will allow ObjectMapper to serialize and
        // deserialize Java 8 date/time types

        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
        RedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // One key can have only one field
        // Everything you will pass as value will get serialized
        // as JSON string which is basically a single value. TTL is applied to individual key
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        // One key can have multiple fields
        // Good for group of related fields
        // TTL is applied to the entire key, not to individual fields
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        //  This is final call to validate and make sure that all the settings
        //  are correctly configured. But not needed here because Spring
        //  container will handle for us.
        //  template.afterPropertiesSet();

        return template;
    }
}

/*
 * Redis Storage Strategy:
 *
 * This Redis configuration uses a Hash structure to store multiple related fields
 * under a single Redis key. For example:
 *
 * Redis Hash:
 *   Key: "user:1"
 *   Fields:
 *     - "firstName" -> "John"
 *     - "lastName"  -> "Doe"
 *     - "age"       -> "30"
 *
 * Spring Code Equivalent:
 *   redisTemplate.opsForHash().put("user:1", "firstName", "John");
 *   redisTemplate.opsForHash().put("user:1", "lastName", "Doe");
 *   redisTemplate.opsForHash().put("user:1", "age", 30);
 *
 *   // To fetch all fields of the user:
 *   Map<Object, Object> userFields = redisTemplate.opsForHash().entries("user:1");
 *
 * -------------------------------------------------------------------
 *
 * Alternative Approach:
 *   Using flat Redis keys for each field:
 *
 *     Key: "user:1:firstName" -> "John"
 *     Key: "user:1:lastName"  -> "Doe"
 *     Key: "user:1:age"       -> "30"
 *
 *   Pros: Per-field TTL, independent keys
 *   Cons: More keys to manage, harder to fetch as a group
 *
 * We prefer the Redis Hash approach here for better structure,
 * grouped access, and memory efficiency.
 */
