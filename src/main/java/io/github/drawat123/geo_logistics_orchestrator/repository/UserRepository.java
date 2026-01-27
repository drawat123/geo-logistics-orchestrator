package io.github.drawat123.geo_logistics_orchestrator.repository;

import io.github.drawat123.geo_logistics_orchestrator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
