package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.AuthRequest;
import io.github.drawat123.geo_logistics_orchestrator.dto.RegisterRequest;
import io.github.drawat123.geo_logistics_orchestrator.model.*;
import io.github.drawat123.geo_logistics_orchestrator.repository.DriverRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.RoleRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.UserRepository;
import io.github.drawat123.geo_logistics_orchestrator.security.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, DriverRepository driverRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String authenticate(AuthRequest request) {
        // 1. Capture the Authentication object (it contains the UserDetails & Roles)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Extract roles from the Authentication object
        // .getAuthorities() returns a list of GrantedAuthority (e.g., "ROLE_ADMIN")
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 3. Pass both username AND roles to the JwtUtil
        return jwtUtil.generateToken(request.email(), roles);
    }

    @Transactional
    @Override
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email is already in use!"); // Better to use a custom exception
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        Role roleEntity = roleRepository.findByRole(request.role())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.role()));

        user.getRoles().add(roleEntity);

        return userRepository.save(user);
    }

    @Transactional // <--- CRITICAL: If any line fails, EVERYTHING rolls back
    @Override
    public Driver registerDriver(RegisterRequest request) {
        // 1. Save User first to generate the ID
        User savedUser = registerUser(request);

        // 2. Create the Driver Profile (Business Identity)
        Driver driver = new Driver();
        driver.setUser(savedUser); // Link them!
        driver.setStatus(DriverStatus.OFFLINE); // Default status
        driver.setLatitude(0.0);
        driver.setLongitude(0.0);

        return driverRepository.save(driver);
    }
}