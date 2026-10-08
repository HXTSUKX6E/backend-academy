package backend.academy.linktracker.scrapper.cache;

import backend.academy.linktracker.scrapper.properties.ValkeyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.support.caching.CacheFrontend;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

public class ClientSideCacheManager implements CacheManager {

    private final CacheFrontend<String, byte[]> frontend;
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final ValkeyProperties props;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, ClientSideLinksCache> caches = new ConcurrentHashMap<>();

    public ClientSideCacheManager(
            CacheFrontend<String, byte[]> frontend,
            RedisTemplate<String, byte[]> redisTemplate,
            ValkeyProperties props,
            ObjectMapper objectMapper) {
        this.frontend = frontend;
        this.redisTemplate = redisTemplate;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(
                name, n -> new ClientSideLinksCache(n, frontend, redisTemplate, props.getLinksTtl(), objectMapper));
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caches.keySet());
    }
}
