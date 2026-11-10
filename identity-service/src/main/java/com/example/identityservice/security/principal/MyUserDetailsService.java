package com.example.identityservice.security.principal;

import com.example.identityservice.models.entities.User;
import com.example.identityservice.models.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseGet(() -> {
                    try {
                        Long id = Long.parseLong(username);
                        return userRepository.findByIdWithRolesAndPermissions(id).orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return buildUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> {
                if (role.getRoleName() != null) {
                    String roleName = role.getRoleName().name();
                    if (!roleName.startsWith("ROLE_")) {
                        roleName = "ROLE_" + roleName;
                    }
                    authorities.add(new SimpleGrantedAuthority(roleName));
                }

                // Nạp tất cả permissions của từng role vào GrantedAuthority (chuẩn pm-xuatban)
                if (role.getPermissions() != null) {
                    role.getPermissions().stream()
                            .filter(p -> p.getDeletedAt() == null && p.getName() != null && !p.getName().isBlank())
                            .forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));
                }
            });
        }

        return MyUserDetails.builder()
                .user(user)
                .authorities(authorities)
                .build();
    }
}
