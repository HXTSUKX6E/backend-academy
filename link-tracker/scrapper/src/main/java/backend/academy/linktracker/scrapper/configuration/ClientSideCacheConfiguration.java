package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.cache.ClientSideCacheManager;
import backend.academy.linktracker.scrapper.properties.ValkeyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.caching.CacheAccessor;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(name = "app.cache.client-side-enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ValkeyProperties.class)
public class ClientSideCacheConfiguration {

    private static final String LINKS_CACHE_PREFIX = "links::";

    @Bean(destroyMethod = "shutdown")
    public RedisClient cscRedisClient(LettuceConnectionFactory connectionFactory) {
        var config = connectionFactory.getStandaloneConfiguration();
        return RedisClient.create(RedisURI.builder()
                .withHost(config.getHostName())
                .withPort(config.getPort())
                .withTimeout(Duration.ofSeconds(2))
                .build());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> cscConnection(RedisClient cscRedisClient) {
        return cscRedisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }

    @Bean
    public CacheFrontend<String, byte[]> linksCacheFrontend(StatefulRedisConnection<String, byte[]> cscConnection) {
        Map<String, byte[]> localMap = new ConcurrentHashMap<>();
        return ClientSideCaching.enable(
                CacheAccessor.forMap(localMap),
                cscConnection,
                TrackingArgs.Builder.enabled().bcast().prefixes(LINKS_CACHE_PREFIX));
    }

    @Bean
    public RedisTemplate<String, byte[]> cscRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.setEnableDefaultSerializer(false);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(
            CacheFrontend<String, byte[]> linksCacheFrontend,
            RedisTemplate<String, byte[]> cscRedisTemplate,
            ValkeyProperties props) {
        ObjectMapper cacheMapper = JsonMapper.builder()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .activateDefaultTypingAsProperty(
                        LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.EVERYTHING, "@class")
                .build()
                .findAndRegisterModules();

        return new ClientSideCacheManager(linksCacheFrontend, cscRedisTemplate, props, cacheMapper);
    }
}
