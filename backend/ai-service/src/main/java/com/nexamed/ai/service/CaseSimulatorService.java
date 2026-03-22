package com.nexamed.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexamed.ai.client.GeminiClient;
//import com.nexamed.ai.client.OpenAIClient;
import com.nexamed.ai.dto.CaseSimRequest;
import com.nexamed.ai.dto.CaseSimResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseSimulatorService {

//    private final OpenAIClient openAIClient;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public CaseSimResponse generateCase(CaseSimRequest request) {

        String systemPrompt = """
                You are an experienced medical educator creating realistic clinical cases.
                Return ONLY valid JSON. Be medically accurate and educationally sound.
                """;

        String ageGender    = request.getPatientAge()      != null ? request.getPatientAge()      : "middle-aged adult";
        String complaint    = request.getChiefComplaint()  != null ? request.getChiefComplaint()  : "unspecified";

        String userPrompt = """
                Create a clinical case simulation for a %s medical student in %s.
                Patient profile: %s
                Chief complaint hint: %s
                
                Return ONLY this JSON structure:
                {
                  "patientProfile": "Age, sex, occupation, relevant history in 2 sentences",
                  "chiefComplaint": "Main presenting complaint",
                  "history": "History of presenting illness in 3-4 sentences",
                  "vitals": ["BP: 120/80", "HR: 72 bpm", "RR: 16", "Temp: 37.0C", "SpO2: 98%%"],
                  "physicalExam": ["Finding 1", "Finding 2", "Finding 3"],
                  "labResults": ["Lab test: result", "Lab test: result"],
                  "differentials": ["Most likely: Diagnosis", "Consider: Diagnosis", "Rule out: Diagnosis"],
                  "teachingPoints": "Key learning points from this case in 2-3 sentences"
                }
                """.formatted(
                request.getDifficulty().toLowerCase(),
                request.getSpecialty(),
                ageGender,
                complaint
        );

        String raw = geminiClient.generate(systemPrompt, userPrompt);
        return parseCase(raw);
    }

    private CaseSimResponse parseCase(String json) {
        try {
            String clean = json.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode node = objectMapper.readTree(clean);

            return CaseSimResponse.builder()
                    .caseId(UUID.randomUUID().toString())
                    .patientProfile(node.path("patientProfile").asText())
                    .chiefComplaint(node.path("chiefComplaint").asText())
                    .history(node.path("history").asText())
                    .vitals(parseStringList(node.path("vitals")))
                    .physicalExam(parseStringList(node.path("physicalExam")))
                    .labResults(parseStringList(node.path("labResults")))
                    .differentials(parseStringList(node.path("differentials")))
                    .teachingPoints(node.path("teachingPoints").asText())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse case simulation JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to generate clinical case. Please try again.");
        }
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(n -> result.add(n.asText()));
        }
        return result;
    }
}