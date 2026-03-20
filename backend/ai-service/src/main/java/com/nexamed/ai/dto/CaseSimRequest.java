package com.nexamed.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaseSimRequest {

    @NotBlank
    private String specialty;          // e.g. "Cardiology", "Emergency Medicine"

    private String difficulty = "INTERMEDIATE";

    private String patientAge;         // optional: "45-year-old male"
    private String chiefComplaint;     // optional: "chest pain"
}