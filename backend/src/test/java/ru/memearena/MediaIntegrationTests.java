package ru.memearena;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.memearena.IntegrationTestSupport.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaIntegrationTests {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.admin-token", () -> ADMIN_TOKEN);
    }

    @Autowired MockMvc mvc;

    @Test
    void uploadContentMemeModerateFlow() throws Exception {
        var session = createGuestAndReturnSession(mvc, "MediaUser");
        var file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1});

        String uploadJson = mvc.perform(multipart("/api/v1/media/images")
                        .file(file)
                        .header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentUrl").exists())
                .andReturn().getResponse().getContentAsString();
        String mediaId = readJsonField(uploadJson, "id");

        var res = mvc.perform(get("/api/v1/media/" + mediaId + "/content"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().exists("ETag"))
                .andReturn().getResponse();
        mvc.perform(get("/api/v1/media/" + mediaId + "/content").header("If-None-Match", res.getHeader("ETag")))
                .andExpect(status().isNotModified());

        String memeJson = mvc.perform(post("/api/v1/memes")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Uploaded\",\"mediaAssetId\":\"" + mediaId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moderationStatus").value("PENDING"))
                .andExpect(jsonPath("$.contentUrl").value("/api/v1/media/" + mediaId + "/content"))
                .andReturn().getResponse().getContentAsString();
        String memeId = readJsonField(memeJson, "id");

        mvc.perform(get("/api/v1/admin/moderation/memes"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/moderation/memes").header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/moderation/memes").header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].moderationStatus").value("PENDING"));
        mvc.perform(post("/api/v1/admin/moderation/memes/" + memeId + "/approve").header("Authorization", adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moderationStatus").value("APPROVED"));
        mvc.perform(get("/api/v1/memes/" + memeId).header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moderationStatus").value("APPROVED"));
    }

    @Test
    void mediaErrors() throws Exception {
        var session = createGuestAndReturnSession(mvc, "ErrUser");
        mvc.perform(multipart("/api/v1/media/images")
                        .file(new MockMultipartFile("file", "e.png", "image/png", new byte[]{}))
                        .header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_FILE"));
        mvc.perform(multipart("/api/v1/media/images")
                        .file(new MockMultipartFile("file", "a.txt", "text/plain", new byte[]{1}))
                        .header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(post("/api/v1/memes")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"imageUrl\":\"https://e/a.png\",\"mediaAssetId\":\"00000000-0000-0000-0000-000000000001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_MEDIA_SOURCE"));
    }
}
