package io.github.drawat123.geo_logistics_orchestrator.security.service;

import io.github.drawat123.geo_logistics_orchestrator.model.User;
import io.github.drawat123.geo_logistics_orchestrator.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // The Single Method: Spring calls this when someone tries to login
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // STEP A: Find the user in YOUR database
        // We use the input (email) to query our repository
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert Set<Role> -> List<GrantedAuthority>
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole().name()))
                .toList();

        // STEP B: Translate it into a format Spring Security understands
        // Spring doesn't know your 'User' class. It only knows 'org.springframework.security.core.userdetails.User'
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
