package backend.academy.linktracker.scrapper.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.support.caching.CacheFrontend;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.core.RedisTemplate;

public class ClientSideLinksCache implements Cache {

    private final String name;
    private final CacheFrontend<String, byte[]> frontend;
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final Duration ttl;
    private final ObjectMapper objectMapper;

    public ClientSideLinksCache(
            String name,
            CacheFrontend<String, byte[]> frontend,
            RedisTemplate<String, byte[]> redisTemplate,
            Duration ttl,
            ObjectMapper objectMapper) {
        this.name = name;
        this.frontend = frontend;
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return frontend;
    }

    @Override
    public ValueWrapper get(Object key) {
        byte[] bytes = frontend.get(redisKey(key));
        if (bytes == null) {
            return null;
        }
        return new SimpleValueWrapper(deserialize(bytes));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        byte[] bytes = frontend.get(redisKey(key));
        if (bytes == null) {
            return null;
        }
        Object value = deserialize(bytes);
        if (value == null || type.isInstance(value)) {
            return (T) value;
        }
        try {
            return objectMapper.convertValue(value, type);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }
        T value;
        try {
            value = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
        put(key, value);
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(value);
            redisTemplate.opsForValue().set(redisKey(key), bytes, ttl);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize value for cache '" + name + "'", e);
        }
    }

    @Override
    public void evict(Object key) {
        // Deleting from Redis triggers server-sent invalidation via CLIENT TRACKING BCAST,
        // which causes Lettuce to remove the entry from the L1 map automatically.
        redisTemplate.delete(redisKey(key));
    }

    @Override
    public void clear() {
        Set<String> keys = redisTemplate.keys(name + "::*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private String redisKey(Object key) {
        return name + "::" + key;
    }

    private Object deserialize(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, Object.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize cache value", e);
        }
    }
}
