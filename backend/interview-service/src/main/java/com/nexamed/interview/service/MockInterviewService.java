package com.nexamed.interview.service;

import com.nexamed.interview.client.AiServiceClient;
import com.nexamed.interview.dto.SessionRequest;
import com.nexamed.interview.dto.SessionResponse;
import com.nexamed.interview.dto.SubmitAnswerRequest;
import com.nexamed.interview.event.InterviewCompletedEvent;
import com.nexamed.interview.model.InterviewQuestion;
import com.nexamed.interview.model.InterviewSession;
import com.nexamed.interview.model.InterviewType;
import com.nexamed.interview.model.SessionStatus;
import com.nexamed.interview.repository.InterviewQuestionRepository;
import com.nexamed.interview.repository.InterviewSessionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockInterviewService {

    private final InterviewSessionRepository sessionRepo;
    private final InterviewQuestionRepository questionRepo;
    private final AiServiceClient             aiServiceClient;
    private final KafkaTemplate<String, InterviewCompletedEvent> kafkaTemplate;
    private final EntityManager               entityManager;

    @Value("${kafka.topics.interview-completed}")
    private String interviewCompletedTopic;

    // Pre-defined question banks per specialty — real app would use AI to generate
    private static final Map<String, List<String>> QUESTION_BANK = Map.of(
            "Cardiology", List.of(
                    "Explain the mechanism of action of beta blockers and their clinical uses.",
                    "How would you manage a patient presenting with acute STEMI?",
                    "What are the main differences between systolic and diastolic heart failure?",
                    "Describe the pathophysiology of atrial fibrillation.",
                    "When would you use anticoagulation therapy in a cardiac patient?"
            ),
            "General Medicine", List.of(
                    "How do you approach a patient presenting with unexplained weight loss?",
                    "Describe the management of a patient with type 2 diabetes.",
                    "What are the red flag symptoms in a patient with headache?",
                    "How would you investigate a patient with suspected anaemia?",
                    "Describe the approach to a febrile patient returning from a tropical country."
            ),
            "Pharmacology", List.of(
                    "Explain the difference between pharmacokinetics and pharmacodynamics.",
                    "What is the mechanism of resistance to penicillin antibiotics?",
                    "Describe the adverse effects of long-term corticosteroid use.",
                    "How do ACE inhibitors work and when are they contraindicated?",
                    "What is the mechanism of action of statins?"
            )
    );

    // ── Start session ─────────────────────────────────────────────

    @Transactional
    public SessionResponse startSession(SessionRequest request, String studentId) {
        String roomId = "mock-" + UUID.randomUUID().toString().substring(0, 8);

        InterviewSession session = InterviewSession.builder()
                .studentId(UUID.fromString(studentId))
                .type(InterviewType.AI_MOCK)
                .status(SessionStatus.IN_PROGRESS)
                .specialty(request.getSpecialty())
                .roomId(roomId)
                .startedAt(LocalDateTime.now())
                .scheduledAt(request.getScheduledAt() != null
                        ? request.getScheduledAt() : LocalDateTime.now())
                .build();

        InterviewSession saved = sessionRepo.saveAndFlush(session);
        entityManager.refresh(saved);

        // Pre-load questions from bank (or generate via AI — future enhancement)
        List<String> questions = getQuestionsForSpecialty(request.getSpecialty());
        int order = 0;
        for (String q : questions.subList(0, Math.min(3, questions.size()))) {
            InterviewQuestion iq = InterviewQuestion.builder()
                    .session(saved)
                    .question(q)
                    .questionOrder(order++)
                    .build();
            questionRepo.save(iq);
        }

        log.info("Mock interview started: sessionId={}, student={}, specialty={}",
                saved.getId(), studentId, request.getSpecialty());

        return toResponse(saved);
    }

    // ── Submit answer and get AI feedback ─────────────────────────

    @Transactional
    public Map<String, Object> submitAnswer(UUID sessionId, SubmitAnswerRequest request,
                                            String studentId) {
        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (!session.getStudentId().equals(UUID.fromString(studentId))) {
            throw new RuntimeException("Unauthorized: not your session");
        }

        // Find the question being answered
        InterviewQuestion question = (request.getQuestionId() != null)
                ? questionRepo.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"))
                : questionRepo.findBySessionIdOrderByQuestionOrder(sessionId)
                .stream()
                .filter(q -> q.getStudentAnswer() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No unanswered questions found"));

        // Call ai-service for evaluation via Feign
        Map<String, Object> evalResult;
        try {
            evalResult = aiServiceClient.evaluateAnswer(Map.of(
                    "question",       question.getQuestion(),
                    "answer",         request.getAnswer(),
                    "expectedTopics", session.getSpecialty() + " key concepts",
                    "specialty",      session.getSpecialty()
            ));
        } catch (Exception e) {
            log.warn("AI eval failed, using default: {}", e.getMessage());
            evalResult = Map.of(
                    "overallScore", 70,
                    "detailedFeedback", "AI evaluation temporarily unavailable.",
                    "modelAnswer", "Please review the topic materials."
            );
        }

        // Save answer + feedback to DB
        question.setStudentAnswer(request.getAnswer());
        question.setQuestionScore(getInt(evalResult, "overallScore"));
        question.setAiFeedback(getString(evalResult, "detailedFeedback"));
        questionRepo.save(question);

        return evalResult;
    }

    // ── Complete session ──────────────────────────────────────────

    @Transactional
    public SessionResponse completeSession(UUID sessionId, String studentId) {
        InterviewSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (!session.getStudentId().equals(UUID.fromString(studentId))) {
            throw new RuntimeException("Unauthorized");
        }

        List<InterviewQuestion> answered = questionRepo
                .findBySessionIdOrderByQuestionOrder(sessionId)
                .stream()
                .filter(q -> q.getQuestionScore() != null)
                .toList();

        // Calculate aggregate scores
        int avgScore = answered.isEmpty() ? 0
                : (int) answered.stream()
                .mapToInt(InterviewQuestion::getQuestionScore)
                .average().orElse(0);

        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        session.setOverallScore(avgScore);
        session.setFeedbackSummary("Completed " + answered.size() + " questions. Average score: " + avgScore + "/100");

        InterviewSession saved = sessionRepo.saveAndFlush(session);
        entityManager.refresh(saved);

        // Publish Kafka event → analytics-service, notification-service will consume
        InterviewCompletedEvent event = InterviewCompletedEvent.builder()
                .eventType("INTERVIEW_COMPLETED")
                .sessionId(saved.getId())
                .studentId(saved.getStudentId())
                .interviewType("AI_MOCK")
                .specialty(saved.getSpecialty())
                .overallScore(avgScore)
                .completedAt(saved.getCompletedAt())
                .build();

        kafkaTemplate.send(interviewCompletedTopic, studentId, event);
        log.info("Interview completed: sessionId={}, score={}", sessionId, avgScore);

        return toResponse(saved);
    }

    // ── Get session history ───────────────────────────────────────

    public List<SessionResponse> getMyHistory(String studentId) {
        return sessionRepo.findByStudentIdOrderByCreatedAtDesc(UUID.fromString(studentId))
                .stream().map(this::toResponse).toList();
    }

    public List<InterviewQuestion> getSessionQuestions(UUID sessionId) {
        return questionRepo.findBySessionIdOrderByQuestionOrder(sessionId);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private List<String> getQuestionsForSpecialty(String specialty) {
        return QUESTION_BANK.getOrDefault(specialty,
                QUESTION_BANK.get("General Medicine"));
    }

    private SessionResponse toResponse(InterviewSession s) {
        return SessionResponse.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .type(s.getType())
                .status(s.getStatus())
                .specialty(s.getSpecialty())
                .roomId(s.getRoomId())
                .overallScore(s.getOverallScore())
                .clarityScore(s.getClarityScore())
                .accuracyScore(s.getAccuracyScore())
                .feedbackSummary(s.getFeedbackSummary())
                .scheduledAt(s.getScheduledAt())
                .startedAt(s.getStartedAt())
                .completedAt(s.getCompletedAt())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        return 0;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}