package com.nexamed.auth.repository;

import com.nexamed.auth.model.AuthProvider;
import com.nexamed.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Used by OAuth2 flow to find or create Google users
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}