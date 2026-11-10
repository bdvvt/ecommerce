package com.example.identityservice.security;

import com.example.identityservice.security.principal.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for Spring Security (theo kiến trúc pm-xuatban).
 */
public final class SecurityUtils {

    public static final String AUTHORITIES_KEY = "auth";

    private SecurityUtils() {
    }

    public static SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }

    public static Authentication getAuthentication() {
        SecurityContext context = getSecurityContext();
        return context != null ? context.getAuthentication() : null;
    }

    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof MyUserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }
        if (authentication.getPrincipal() instanceof String s) {
            return Optional.of(s);
        }
        return Optional.empty();
    }

    public static Long getLoggedUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof MyUserDetails myUserDetails) {
            return myUserDetails.getUser() != null ? myUserDetails.getUser().getUserId() : null;
        }
        if (authentication.getPrincipal() instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static List<String> getRoles() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return Collections.emptyList();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .distinct()
                .collect(Collectors.toList());
    }

    public static Set<String> getAuthorities() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return Collections.emptySet();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    public static boolean hasCurrentUserThisAuthority(String authority) {
        Authentication authentication = getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        Set<String> authSet = new HashSet<>(Arrays.asList(authorities));
        return authentication.getAuthorities().stream().anyMatch(a -> authSet.contains(a.getAuthority()));
    }
}
