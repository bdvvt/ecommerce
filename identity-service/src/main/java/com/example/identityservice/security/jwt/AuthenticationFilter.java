package com.example.identityservice.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.example.identityservice.security.principal.MyUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final MyUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String xAuthUserId = request.getHeader("X-Auth-UserId");
        String xAuthRoles = request.getHeader("X-Auth-Roles");
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(xAuthUserId)) {
            List<GrantedAuthority> authorities = Collections.emptyList();
            if (StringUtils.hasText(xAuthRoles)) {
                authorities = Arrays.stream(xAuthRoles.replace("[", "").replace("]", "").split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UserDetails userDetails = null;
            try {
                userDetails = userDetailsService.loadUserByUsername(xAuthUserId);
            } catch (Exception ignored) {
            }

            Object principal = userDetails != null ? userDetails : xAuthUserId;
            List<GrantedAuthority> finalAuthorities = (userDetails != null && !userDetails.getAuthorities().isEmpty())
                    ? (List<GrantedAuthority>) userDetails.getAuthorities()
                    : authorities;

            Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, finalAuthorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtils.isTokenValid(token)) {
                    String username = jwtUtils.extractUsername(token);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}
