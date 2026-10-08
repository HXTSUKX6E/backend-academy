package backend.academy.linktracker.scrapper.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import io.lettuce.core.support.caching.CacheFrontend;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@Tag("integration")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.cache.client-side-enabled=true")
class ClientSideCacheTest {

    private static final String LINKS_CACHE = "links";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CacheFrontend<String, byte[]> linksCacheFrontend;

    @Autowired
    RedisTemplate<String, byte[]> cscRedisTemplate;

    @MockitoSpyBean
    LinkRepository linkRepository;

    @BeforeEach
    void resetState() {
        clearInvocations(linkRepository);
    }

    @Test
    void getLinks_populatesL1AfterFirstCacheMiss() throws Exception {
        long chatId = 500L;
        mockMvc.perform(post("/tg-chat/" + chatId)).andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/csc/test\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());
        clearInvocations(linkRepository);

        String redisKey = LINKS_CACHE + "::" + chatId;

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isOk());
        verify(linkRepository, times(1)).getLinksByChat(chatId);

        assertThat(linksCacheFrontend.get(redisKey)).isNotNull();

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isOk());
        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isOk());

        verify(linkRepository, times(1)).getLinksByChat(chatId);
    }

    @Test
    void serverAssistedInvalidation_removesEntryFromL1() throws Exception {
        long chatId = 501L;
        mockMvc.perform(post("/tg-chat/" + chatId)).andExpect(status().isOk());
        clearInvocations(linkRepository);

        String redisKey = LINKS_CACHE + "::" + chatId;

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isOk());
        linksCacheFrontend.get(redisKey);

        cscRedisTemplate.delete(redisKey);

        // Lettuce обрабатывает инвалидацию асинхронно через CLIENT TRACKING BCAST
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> assertThat(linksCacheFrontend.get(redisKey))
                .isNull());
    }

    @Test
    void cacheEvict_invalidatesBothL1AndL2() throws Exception {
        long chatId = 502L;
        mockMvc.perform(post("/tg-chat/" + chatId)).andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/csc/evict\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());
        clearInvocations(linkRepository);

        String redisKey = LINKS_CACHE + "::" + chatId;

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isOk());
        linksCacheFrontend.get(redisKey);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/csc/evict2\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        assertThat(cscRedisTemplate.hasKey(redisKey)).isFalse();

        // Lettuce обрабатывает инвалидацию асинхронно через CLIENT TRACKING BCAST
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> assertThat(linksCacheFrontend.get(redisKey))
                .isNull());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isOk());
        verify(linkRepository, times(2)).getLinksByChat(chatId);
    }
}
