package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.ValkeyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(ValkeyProperties.class)
public class ValkeyConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.cache.client-side-enabled", havingValue = "false")
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ValkeyProperties props) {
        ObjectMapper cacheMapper = JsonMapper.builder()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .activateDefaultTypingAsProperty(
                        LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, "@class")
                .build()
                .findAndRegisterModules();

        RedisSerializer<Object> valueSerializer = new JsonRedisSerializer(cacheMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(props.getLinksTtl())
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    private static final class JsonRedisSerializer implements RedisSerializer<Object> {

        private final ObjectMapper objectMapper;

        JsonRedisSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public byte[] serialize(Object value) throws SerializationException {
            if (value == null) {
                return new byte[0];
            }
            try {
                return objectMapper.writeValueAsBytes(value);
            } catch (Exception e) {
                throw new SerializationException("Could not serialize cache value", e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return objectMapper.readValue(bytes, Object.class);
            } catch (Exception e) {
                throw new SerializationException("Could not deserialize cache value", e);
            }
        }
    }
}
