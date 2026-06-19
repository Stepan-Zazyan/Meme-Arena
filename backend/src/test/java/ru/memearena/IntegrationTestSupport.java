package ru.memearena;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class IntegrationTestSupport {
    static final String ADMIN_TOKEN = "test-admin-token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private IntegrationTestSupport() {}

    record GuestSession(String userId, String accessToken) {}

    static GuestSession createGuestAndReturnSession(MockMvc mvc, String nickname) throws Exception {
        String json = mvc.perform(post("/api/v1/users/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new GuestSession(readNestedJsonField(json, "user", "id"), readJsonField(json, "accessToken"));
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }

    static String adminBearer() {
        return bearer(ADMIN_TOKEN);
    }

    static String readJsonField(String json, String field) throws Exception {
        JsonNode node = MAPPER.readTree(json).path(field);
        if (node.isMissingNode() || node.isNull()) throw new IllegalStateException(json);
        return node.asText();
    }

    static String readNestedJsonField(String json, String object, String field) throws Exception {
        JsonNode node = MAPPER.readTree(json).path(object).path(field);
        if (node.isMissingNode() || node.isNull()) throw new IllegalStateException(json);
        return node.asText();
    }
}
