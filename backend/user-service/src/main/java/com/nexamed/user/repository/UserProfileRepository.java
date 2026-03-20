package com.nexamed.user.repository;

import com.nexamed.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByUserId(UUID userId);
}