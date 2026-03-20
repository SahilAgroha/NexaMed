package com.nexamed.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CaseSimResponse {
    private String       caseId;
    private String       patientProfile;    // age, sex, history
    private String       chiefComplaint;
    private String       history;
    private List<String> vitals;
    private List<String> physicalExam;
    private List<String> labResults;
    private List<String> differentials;    // possible diagnoses student should consider
    private String       teachingPoints;
}