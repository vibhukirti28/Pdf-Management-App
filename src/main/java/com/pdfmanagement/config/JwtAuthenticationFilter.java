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

/**
 * JwtAuthenticationFilter is a Spring Security filter that intercepts HTTP requests to perform JWT-based authentication.
 * <p>
 * This filter extends {@link OncePerRequestFilter} to ensure it is executed once per request. It checks for the presence
 * of a JWT Bearer token in the Authorization header, validates it, and sets the authentication in the SecurityContext
 * if the token is valid and corresponds to a known user.
 * <p>
 * Public endpoints (such as login, registration, and shared resource access) are excluded from JWT validation.
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Skip JWT validation for configured public endpoints.</li>
 *   <li>Extract and validate JWT from the Authorization header for protected endpoints.</li>
 *   <li>Extract user email from the JWT and load user details from the repository.</li>
 *   <li>Validate the JWT against the user's username.</li>
 *   <li>Set the authenticated user in the SecurityContext if validation succeeds.</li>
 *   <li>Log key steps and decisions for debugging and traceability.</li>
 * </ul>
 * <p>
 * Dependencies:
 * <ul>
 *   <li>{@link JwtUtil} for JWT parsing and validation.</li>
 *   <li>{@link UserRepository} for loading user details by email.</li>
 * </ul>
 * <p>
 * If the JWT is missing, malformed, or invalid, the filter allows the request to proceed without authentication,
 * leaving further handling to downstream filters or controllers.
 *
 * @author [Your Name]
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    /**
     * Filters incoming HTTP requests to handle JWT-based authentication.
     * <p>
     * This method intercepts each request and performs the following:
     * <ul>
     *   <li>Logs the incoming request method and URI for debugging purposes.</li>
     *   <li>Skips JWT validation for public endpoints such as login, registration, shared access, and PDF search.</li>
     *   <li>Checks for the presence and validity of the "Authorization" header with a Bearer token.</li>
     *   <li>If a valid JWT is present, extracts the user's email and attempts to authenticate the user by:
     *     <ul>
     *       <li>Loading user details from the repository using the extracted email.</li>
     *       <li>Validating the JWT against the user's username.</li>
     *       <li>Setting the authentication in the Spring Security context if validation succeeds.</li>
     *     </ul>
     *   </li>
     *   <li>If authentication is not possible (e.g., missing/invalid token, user not found), the request proceeds without authentication.</li>
     *   <li>Always passes the request and response to the next filter in the chain.</li>
     * </ul>
     *
     * @param request      the HTTP servlet request
     * @param response     the HTTP servlet response
     * @param filterChain  the filter chain to pass the request/response to the next filter
     * @throws ServletException if an exception occurs during filtering
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPathInfo = request.getMethod() + " " + request.getRequestURI();
        System.out.println("[JwtAuthFilter] Processing request: " + requestPathInfo);

        String path = request.getServletPath();


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
                } 
                
                else {
                    System.out.println("[JwtAuthFilter] JWT validation FAILED for " + email + " for " + requestPathInfo);
                }

            } // Closes the 'else' for 'userDetails != null'
        } 
        
        else if (email != null) {
             System.out.println("[JwtAuthFilter] SecurityContext already has authentication for " + requestPathInfo + ". Current auth: " + SecurityContextHolder.getContext().getAuthentication().getName());
        } 
        
        else {
            // This case not reached if email extraction failed and returned above
            System.out.println("[JwtAuthFilter] Email is null after extraction attempt for " + requestPathInfo + ", cannot authenticate.");
        }

        filterChain.doFilter(request, response);
    }
}
