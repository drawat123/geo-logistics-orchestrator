package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.AuthRequest;
import io.github.drawat123.geo_logistics_orchestrator.dto.RegisterRequest;
import io.github.drawat123.geo_logistics_orchestrator.model.Driver;
import io.github.drawat123.geo_logistics_orchestrator.model.User;

public interface AuthService {
    String authenticate(AuthRequest request);

    User registerUser(RegisterRequest request);

    Driver registerDriver(RegisterRequest request);
}
