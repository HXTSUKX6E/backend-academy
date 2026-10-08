package backend.academy.linktracker.bot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.bot.controller.UpdatesController;
import backend.academy.linktracker.bot.grpc.UpdateNotifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UpdatesController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UpdatesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private UpdateNotifier botGrpcUpdateNotifier;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.telegram.url", () -> "http://localhost:8089/bot");
    }

    @Test
    void validRequest_returnsOk() throws Exception {
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "id": 1,
                      "url": "https://github.com/a/b",
                      "description": "updated",
                      "tgChatIds": [1,2]
                    }
                    """))
                .andExpect(status().isOk());
    }

    @Test
    void invalidRequest_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidRequest_wrongFieldTypes_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "id": "not-a-number",
                      "url": 123,
                      "description": true,
                      "tgChatIds": ["chat"]
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidRequest_missingChatIds_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "id": 1,
                      "url": "https://github.com/a/b",
                      "description": "updated"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
}
