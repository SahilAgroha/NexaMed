package com.nexamed.interview.repository;

import com.nexamed.interview.model.InterviewSession;
import com.nexamed.interview.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {
    List<InterviewSession> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
    List<InterviewSession> findByInterviewerIdAndStatus(UUID interviewerId, SessionStatus status);
    List<InterviewSession> findByStudentIdAndStatus(UUID studentId, SessionStatus status);
}