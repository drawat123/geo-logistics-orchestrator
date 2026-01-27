package io.github.drawat123.geo_logistics_orchestrator.dto;

import io.github.drawat123.geo_logistics_orchestrator.model.ERole;

public record RegisterRequest(
        String username,
        String email,
        String password,
        ERole role
) {
}