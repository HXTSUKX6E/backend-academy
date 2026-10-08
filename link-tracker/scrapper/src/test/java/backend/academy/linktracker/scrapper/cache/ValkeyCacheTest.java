package backend.academy.linktracker.scrapper.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Tag("integration")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ValkeyCacheTest {

    private static final String LINKS_CACHE = "links";
    private static final long CHAT_ID = 200L;
    private static final long CHAT_ID_2 = 201L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CacheManager cacheManager;

    @MockitoSpyBean
    LinkRepository linkRepository;

    @BeforeEach
    void resetState() {
        var cache = cacheManager.getCache(LINKS_CACHE);
        if (cache != null) {
            cache.clear();
        }
        clearInvocations(linkRepository);
    }

    @Test
    void getLinks_resultIsCachedAfterFirstCall() throws Exception {
        mockMvc.perform(post("/tg-chat/" + CHAT_ID)).andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", CHAT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/cache-test\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        MvcResult first = mockMvc.perform(get("/links").header("Tg-Chat-Id", CHAT_ID))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult second = mockMvc.perform(get("/links").header("Tg-Chat-Id", CHAT_ID))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(first.getResponse().getContentAsString())
                .isEqualTo(second.getResponse().getContentAsString());

        verify(linkRepository, times(1)).getLinksByChat(CHAT_ID);
    }

    @Test
    void addLink_invalidatesCache() throws Exception {
        mockMvc.perform(post("/tg-chat/" + CHAT_ID_2)).andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", CHAT_ID_2)).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", CHAT_ID_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/new-link\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/links").header("Tg-Chat-Id", CHAT_ID_2))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("new-link");

        verify(linkRepository, times(2)).getLinksByChat(CHAT_ID_2);
    }

    @Test
    void removeLink_invalidatesCache() throws Exception {
        long chatId = 202L;
        mockMvc.perform(post("/tg-chat/" + chatId)).andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/to-remove\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/to-remove\"}"))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain("to-remove");

        verify(linkRepository, times(2)).getLinksByChat(chatId);
    }

    @Test
    void cacheIsolatedPerChat() throws Exception {
        long chatA = 210L;
        long chatB = 211L;
        mockMvc.perform(post("/tg-chat/" + chatA)).andExpect(status().isOk());
        mockMvc.perform(post("/tg-chat/" + chatB)).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/chat-a-link\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatA)).andExpect(status().isOk());
        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatB)).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/chat-a-link-2\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatB)).andExpect(status().isOk());

        verify(linkRepository, times(1)).getLinksByChat(chatB);
    }
}
