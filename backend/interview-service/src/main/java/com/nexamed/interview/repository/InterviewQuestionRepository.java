package com.nexamed.interview.repository;

import com.nexamed.interview.model.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {
    List<InterviewQuestion> findBySessionIdOrderByQuestionOrder(UUID sessionId);
}