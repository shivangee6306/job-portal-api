package com.shivangee.jobportal.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobPortalFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken(String role) throws Exception {
        String email = UUID.randomUUID() + "@test.com";
        String body = String.format(
                "{\"name\":\"Test User\",\"email\":\"%s\",\"password\":\"secret123\",\"role\":\"%s\"}", email, role);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createJob(String recruiterToken, String title) throws Exception {
        String body = String.format(
                "{\"title\":\"%s\",\"description\":\"Build APIs\",\"company\":\"Acme\","
                        + "\"location\":\"Noida\",\"salary\":500000}", title);
        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    @Test
    void publicCanBrowseJobs() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void homeRedirectsToSwaggerWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/swagger-ui.html"));
    }

    @Test
    void creatingJobWithoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"description\":\"y\",\"company\":\"z\",\"location\":\"w\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void candidateCannotCreateJob_returns403() throws Exception {
        String candidateToken = registerAndGetToken("CANDIDATE");
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"description\":\"y\",\"company\":\"z\",\"location\":\"w\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidJobBody_returns400WithFieldErrors() throws Exception {
        String recruiterToken = registerAndGetToken("RECRUITER");
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"y\",\"company\":\"z\",\"location\":\"w\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    void fullHiringFlow_postApplyShortlist() throws Exception {
        String recruiterToken = registerAndGetToken("RECRUITER");
        String candidateToken = registerAndGetToken("CANDIDATE");
        String uniqueTitle = "Zebra-Engineer-" + UUID.randomUUID();

        long jobId = createJob(recruiterToken, uniqueTitle);

        // job is searchable
        mockMvc.perform(get("/api/jobs").param("keyword", uniqueTitle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        // candidate applies
        MvcResult applied = mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"I love Java\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andReturn();
        long applicationId = objectMapper.readTree(applied.getResponse().getContentAsString()).get("id").asLong();

        // duplicate apply is rejected
        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\":\"again\"}"))
                .andExpect(status().isConflict());

        // recruiter sees the application and shortlists it
        mockMvc.perform(get("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        // candidate sees the new status
        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("SHORTLISTED"));
    }
}
