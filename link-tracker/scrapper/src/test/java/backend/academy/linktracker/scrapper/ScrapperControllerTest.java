package backend.academy.linktracker.scrapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Tag("integration")
@SpringBootTest
@org.springframework.context.annotation.Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class ScrapperControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void scenario31_addAndGetLink() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo\",\"tags\":[\"work\"],\"filters\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://github.com/user/repo")));
    }

    @Test
    void scenario32_addAndDeleteLink() throws Exception {
        mockMvc.perform(post("/tg-chat/10")).andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo-delete\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo-delete\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("repo-delete"))));
    }

    @Test
    void scenario33_deleteFromMissingChat_failsAndOriginalStillExists() throws Exception {
        mockMvc.perform(post("/tg-chat/20")).andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo-keep\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo-keep\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("repo-keep")));
    }

    @Test
    void scenario34_addLinkToMissingChat_fails() throws Exception {
        mockMvc.perform(post("/tg-chat/30")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void scenario35_deletedChatCannotBeUsed() throws Exception {
        mockMvc.perform(post("/tg-chat/40")).andExpect(status().isOk());
        mockMvc.perform(delete("/tg-chat/40")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "40")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo\",\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void scenario36_deleteMissingChat_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/tg-chat/777")).andExpect(status().isNotFound());
    }

    @Test
    void duplicateLink_returnsConflict() throws Exception {
        mockMvc.perform(post("/tg-chat/50")).andExpect(status().isOk());
        String body = "{\"link\":\"https://github.com/user/repo\",\"tags\":[],\"filters\":[]}";
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void addLink_missingRequiredField_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/tg-chat/60")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", "60")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tags\":[],\"filters\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void linksWithoutHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/links")).andExpect(status().isBadRequest());
    }
}
