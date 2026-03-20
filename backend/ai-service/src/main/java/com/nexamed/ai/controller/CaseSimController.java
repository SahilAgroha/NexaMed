package com.nexamed.ai.controller;

import com.nexamed.ai.dto.CaseSimRequest;
import com.nexamed.ai.dto.CaseSimResponse;
import com.nexamed.ai.service.CaseSimulatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/cases")
@RequiredArgsConstructor
public class CaseSimController {

    private final CaseSimulatorService caseService;

    /**
     * POST /api/ai/cases/generate
     * Body: { specialty, difficulty, patientAge?, chiefComplaint? }
     * Returns: full clinical case with vitals, labs, differentials
     */
    @PostMapping("/generate")
    public ResponseEntity<CaseSimResponse> generateCase(
            @Valid @RequestBody CaseSimRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(caseService.generateCase(request));
    }
}