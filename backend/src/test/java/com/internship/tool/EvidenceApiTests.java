package com.internship.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.PasswordResetToken;
import com.internship.tool.entity.User;
import com.internship.tool.repository.PasswordResetTokenRepository;
import com.internship.tool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:evidence-api-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "app.upload.dir=target/test-uploads",
        "spring.mail.host="
})
class EvidenceApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Test
    void protectedEndpointRequiresJwt() throws Exception {
        mockMvc.perform(get("/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/all").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid JWT")));
    }

    @Test
    void authCreateSearchDashboardExportAndUserIsolationWork() throws Exception {
        String userA = "user-a-" + UUID.randomUUID() + "@example.com";
        String userB = "user-b-" + UUID.randomUUID() + "@example.com";
        String tokenA = registerAndLogin(userA);
        String tokenB = registerAndLogin(userB);

        long alphaId = createEvidence(tokenA, "Alpha Isolated Laptop", "Digital", "ACTIVE");
        createEvidence(tokenB, "Beta Private Report", "Document", "PENDING");

        mockMvc.perform(get("/all").header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alpha Isolated Laptop")))
                .andExpect(content().string(not(containsString("Beta Private Report"))));

        mockMvc.perform(get("/search")
                        .param("q", "Alpha")
                        .param("type", "Digital")
                        .param("status", "ACTIVE")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alpha Isolated Laptop"));

        mockMvc.perform(get("/search")
                        .param("q", "Alpha")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(put("/" + alphaId)
                        .header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(evidenceJson("Hijack Attempt", "Digital", "ACTIVE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard").header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.active").value(1));

        mockMvc.perform(get("/analytics").header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type.Digital").value(1));

        mockMvc.perform(get("/export").header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alpha Isolated Laptop")))
                .andExpect(content().string(not(containsString("Beta Private Report"))));

        mockMvc.perform(delete("/" + alphaId).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk());
    }

    @Test
    void uploadValidatesAndCreatesEvidenceForCurrentUser() throws Exception {
        String username = "upload-" + UUID.randomUUID() + "@example.com";
        String token = registerAndLogin(username);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "sample evidence".getBytes()
        );

        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .param("name", "Uploaded Note")
                        .param("type", "Document")
                        .param("status", "PENDING")
                        .param("priority", "HIGH")
                        .param("assignedTo", "Upload Tester")
                        .param("deadline", "2026-05-08")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fileName").exists());

        mockMvc.perform(get("/all").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[0].assignedTo").value("Upload Tester"))
                .andExpect(jsonPath("$.content[0].deadline").value("2026-05-08"));
    }

    @Test
    void forgotAndResetPasswordFlowWorks() throws Exception {
        String username = "reset-" + UUID.randomUUID();
        String email = username + "@example.com";
        register(username, email, "password");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("If an account exists")));

        PasswordResetToken token = resetTokenRepository.findAll().stream()
                .filter(resetToken -> resetToken.getUser().getEmail().equals(email))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token.getToken(),
                                "newPassword", "new-password"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "new-password"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "not-a-real-token",
                                "newPassword", "another-password"
                        ))))
                .andExpect(status().isBadRequest());

        User user = userRepository.findByEmail(email).orElseThrow();
        PasswordResetToken expired = new PasswordResetToken();
        expired.setToken(UUID.randomUUID().toString());
        expired.setUser(user);
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        expired.setUsed(false);
        resetTokenRepository.save(expired);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", expired.getToken(),
                                "newPassword", "expired-password"
                        ))))
                .andExpect(status().isBadRequest());
    }

    private String registerAndLogin(String username) throws Exception {
        String payload = register(username, username, "password");

        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "password"
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String register(String username, String email, String password) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "email", email,
                "phoneNumber", "9876543210",
                "password", password
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        return payload;
    }

    private long createEvidence(String token, String name, String type, String status) throws Exception {
        String response = mockMvc.perform(post("/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(evidenceJson(name, type, status)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private String evidenceJson(String name, String type, String status) throws Exception {
        return objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("name", name),
                Map.entry("type", type),
                Map.entry("status", status),
                Map.entry("priority", "HIGH"),
                Map.entry("caseNumber", "CASE-" + UUID.randomUUID()),
                Map.entry("caseName", "Isolation Test"),
                Map.entry("department", "Cyber Crime"),
                Map.entry("assignedTo", "Tester"),
                Map.entry("dateCollected", "2026-05-01"),
                Map.entry("deadline", "2026-05-05"),
                Map.entry("location", "Lab"),
                Map.entry("source", "Unit Test"),
                Map.entry("description", "Created by MockMvc test"),
                Map.entry("tags", "test")
        ));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
