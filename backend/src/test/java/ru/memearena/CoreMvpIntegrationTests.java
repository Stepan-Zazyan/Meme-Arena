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
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test","local"})
class CoreMvpIntegrationTests {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry){ registry.add("spring.datasource.url", POSTGRES::getJdbcUrl); registry.add("spring.datasource.username", POSTGRES::getUsername); registry.add("spring.datasource.password", POSTGRES::getPassword); }
    @Autowired MockMvc mvc;
    @Test void guestMemeBattleVoteAndTopFlow() throws Exception {
        String u=mvc.perform(post("/api/v1/users/guest").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"Stepan\"}"))
                .andExpect(status().isCreated()).andExpect(header().exists("X-Request-Id")).andExpect(jsonPath("$.status").value("NEWBIE")).andReturn().getResponse().getContentAsString();
        String userId=extract(u,"id");
        mvc.perform(post("/api/v1/users/guest").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"stepan\"}")) .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("NICKNAME_ALREADY_EXISTS"));
        mvc.perform(post("/api/v1/users/guest").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"x\"}")) .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        String m=mvc.perform(post("/api/v1/memes").contentType(MediaType.APPLICATION_JSON).content("{\"uploaderId\":\""+userId+"\",\"title\":\"New meme\",\"imageUrl\":\"https://example.com/m.jpg\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.moderationStatus").value("PENDING")).andReturn().getResponse().getContentAsString();
        String memeId=extract(m,"id");
        mvc.perform(get("/api/v1/users/"+userId)).andExpect(status().isOk()).andExpect(jsonPath("$.submittedMemesCount").value(1));
        String b=mvc.perform(get("/api/v1/battles/next").param("userId",userId)).andExpect(status().isOk()).andExpect(jsonPath("$.left.id").exists()).andExpect(jsonPath("$.right.id").exists()).andReturn().getResponse().getContentAsString();
        String left=extractPath(b,"left","id"), right=extractPath(b,"right","id");
        mvc.perform(post("/api/v1/votes").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\""+userId+"\",\"leftMemeId\":\""+left+"\",\"rightMemeId\":\""+right+"\",\"winnerMemeId\":\""+ UUID.randomUUID()+"\"}")) .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_WINNER"));
        mvc.perform(post("/api/v1/votes").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\""+userId+"\",\"leftMemeId\":\""+left+"\",\"rightMemeId\":\""+right+"\",\"winnerMemeId\":\""+left+"\"}")) .andExpect(status().isCreated()).andExpect(jsonPath("$.winner.ratingAfter").value(1516));
        mvc.perform(post("/api/v1/votes").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\""+userId+"\",\"leftMemeId\":\""+left+"\",\"rightMemeId\":\""+right+"\",\"winnerMemeId\":\""+left+"\"}")) .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PAIR_ALREADY_VOTED"));
        mvc.perform(get("/api/v1/memes/top").param("period","DAY")).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].periodBattles").value(1));
        mvc.perform(post("/api/v1/local/memes/"+memeId+"/approve")).andExpect(status().isOk()).andExpect(jsonPath("$.moderationStatus").value("APPROVED"));
    }
    @Test void unknownIdsReturn404() throws Exception { mvc.perform(get("/api/v1/users/"+UUID.randomUUID())).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("USER_NOT_FOUND")); mvc.perform(get("/api/v1/memes/"+UUID.randomUUID())).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("MEME_NOT_FOUND")); }
    private static String extract(String json,String field){var m=Pattern.compile("\\\""+field+"\\\":\\\"([^\\\"]+)\\\"").matcher(json); if(!m.find()) throw new IllegalStateException(json); return m.group(1);} private static String extractPath(String json,String obj,String field){var m=Pattern.compile("\\\""+obj+"\\\":\\{[^}]*\\\""+field+"\\\":\\\"([^\\\"]+)\\\"").matcher(json); if(!m.find()) throw new IllegalStateException(json); return m.group(1);} }
