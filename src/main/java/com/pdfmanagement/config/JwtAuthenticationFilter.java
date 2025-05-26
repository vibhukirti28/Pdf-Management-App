package com.pdfmanagement.config;

import com.pdfmanagement.util.JwtUtil;
import com.pdfmanagement.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPathInfo = request.getMethod() + " " + request.getRequestURI();
        System.out.println("[JwtAuthFilter] Processing request: " + requestPathInfo);

        String path = request.getServletPath();

        // ✅ Skip JWT validation for public endpoints
        // Public paths from SecurityConfig:
        // "/api/auth/**" -> covered by /api/auth/login, /api/auth/register
        // "/api/shared/access/**" -> covered by /api/shared/access/
        // "/api/shared/download/**"
        // "/api/shared/view/**"
        // "/api/shared/*/comments"
        // "/api/pdf/search"
        if (path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/shared/access/") ||
            path.startsWith("/api/shared/download/") || 
            path.startsWith("/api/shared/view/") ||   
            path.matches("/api/shared/[^/]+/comments") || 
            path.startsWith("/api/pdf/search")) {
            System.out.println("[JwtAuthFilter] Path " + path + " is public, skipping JWT validation.");
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // ✅ Continue without auth if missing or malformed header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JwtAuthFilter] No/Invalid Authorization Bearer header for " + requestPathInfo + ". Passing to next filter.");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        System.out.println("[JwtAuthFilter] Extracted JWT for " + requestPathInfo + ": " + (jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt));

        String email = null;
        try {
            email = jwtUtil.extractEmail(jwt);
            System.out.println("[JwtAuthFilter] Extracted email from JWT for " + requestPathInfo + ": " + email);
        } catch (Exception e) {
            System.err.println("[JwtAuthFilter] Error extracting email from JWT for " + requestPathInfo + ": " + e.getMessage());
            filterChain.doFilter(request, response); // Crucial: proceed if token is malformed after Bearer prefix
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("[JwtAuthFilter] Attempting to authenticate user: " + email + " for " + requestPathInfo);
            UserDetails userDetails = userRepository.findByEmail(email).orElse(null);

            if (userDetails == null) {
                System.out.println("[JwtAuthFilter] UserDetails not found for email: " + email + " for " + requestPathInfo);
            } else { // userDetails is not null here
                System.out.println("[JwtAuthFilter] UserDetails found for " + email + ". Username from DB: " + userDetails.getUsername());
                
                boolean isTokenValid = false;
                try {
                    isTokenValid = jwtUtil.validateToken(jwt, userDetails.getUsername());
                } catch (Exception e) {
                    System.err.println("[JwtAuthFilter] Exception during JWT validation for " + email + " for " + requestPathInfo + ": " + e.getMessage());
                }

                if (isTokenValid) {
                    System.out.println("[JwtAuthFilter] JWT validated successfully for " + email + " for " + requestPathInfo);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("[JwtAuthFilter] Successfully authenticated and set SecurityContext for user: " + authToken.getName() + " for " + requestPathInfo);
                } else {
                    System.out.println("[JwtAuthFilter] JWT validation FAILED for " + email + " for " + requestPathInfo);
                }
            } // Closes the 'else' for 'userDetails != null'
        } else if (email != null) {
             System.out.println("[JwtAuthFilter] SecurityContext already has authentication for " + requestPathInfo + ". Current auth: " + SecurityContextHolder.getContext().getAuthentication().getName());
        } else {
            // This case should ideally not be reached if email extraction failed and returned above
            System.out.println("[JwtAuthFilter] Email is null after extraction attempt for " + requestPathInfo + ", cannot authenticate.");
        }

        filterChain.doFilter(request, response);
    }
}
