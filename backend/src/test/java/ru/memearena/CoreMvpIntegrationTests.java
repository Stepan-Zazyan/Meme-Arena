package ru.memearena;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.memearena.IntegrationTestSupport.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "local"})
class CoreMvpIntegrationTests {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.admin-token", () -> ADMIN_TOKEN);
    }

    @Autowired MockMvc mvc;

    @Test
    void guestMemeBattleVoteAndTopFlow() throws Exception {
        var session = createGuestAndReturnSession(mvc, "Stepan");

        mvc.perform(post("/api/v1/users/guest").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"stepan\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NICKNAME_ALREADY_EXISTS"));
        mvc.perform(post("/api/v1/users/guest").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        String memeJson = mvc.perform(post("/api/v1/memes")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New meme\",\"imageUrl\":\"https://example.com/m.jpg\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moderationStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String memeId = readJsonField(memeJson, "id");

        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(session.userId()))
                .andExpect(jsonPath("$.submittedMemesCount").value(1));

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

        String battleJson = mvc.perform(get("/api/v1/battles/next").header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.left.id").exists())
                .andExpect(jsonPath("$.right.id").exists())
                .andReturn().getResponse().getContentAsString();
        String left = readNestedJsonField(battleJson, "left", "id");
        String right = readNestedJsonField(battleJson, "right", "id");

        mvc.perform(post("/api/v1/votes")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leftMemeId\":\"" + left + "\",\"rightMemeId\":\"" + right + "\",\"winnerMemeId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WINNER"));
        mvc.perform(post("/api/v1/votes")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leftMemeId\":\"" + left + "\",\"rightMemeId\":\"" + right + "\",\"winnerMemeId\":\"" + left + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.winner.ratingAfter").value(1516));
        mvc.perform(post("/api/v1/votes")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leftMemeId\":\"" + left + "\",\"rightMemeId\":\"" + right + "\",\"winnerMemeId\":\"" + left + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAIR_ALREADY_VOTED"));
        mvc.perform(get("/api/v1/memes/top").param("period", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].periodBattles").value(1));
    }

    @Test
    void unknownIdsReturn404() throws Exception {
        var session = createGuestAndReturnSession(mvc, "UnknownsUser");
        mvc.perform(get("/api/v1/users/" + UUID.randomUUID()).header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        mvc.perform(get("/api/v1/memes/" + UUID.randomUUID()).header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEME_NOT_FOUND"));
    }

    @Test
    void protectedEndpointsRequireValidAuthentication() throws Exception {
        var session = createGuestAndReturnSession(mvc, "SecurityUser");
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/users/me/logout").header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCESS_TOKEN_REVOKED"));
    }
}
