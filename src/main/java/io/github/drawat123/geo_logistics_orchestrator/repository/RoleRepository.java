package io.github.drawat123.geo_logistics_orchestrator.repository;

import io.github.drawat123.geo_logistics_orchestrator.model.ERole;
import io.github.drawat123.geo_logistics_orchestrator.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    // Finds the Role entity (ID: 1, Name: ROLE_ADMIN) by its Enum name
    Optional<Role> findByRole(ERole role);
}
