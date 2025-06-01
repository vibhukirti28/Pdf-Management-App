package com.pdfmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Constructs a new {@code SecurityConfig} with the specified JWT authentication filter and user details service.
     *
     * @param jwtAuthenticationFilter the filter responsible for processing JWT authentication in incoming requests
     * @param userDetailsService the service used to retrieve user-specific data for authentication and authorization
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Creates and returns a {@link PasswordEncoder} bean that uses the BCrypt hashing algorithm.
     * <p>
     * BCrypt is a strong and adaptive hashing function recommended for securely storing passwords.
     * </p>
     *
     * @return a {@code PasswordEncoder} instance using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the application's security filter chain.
     * <p>
     * This method sets up HTTP security for the application, including:
     * <ul>
     *   <li>Disabling CSRF protection (suitable for stateless APIs).</li>
     *   <li>Defining public endpoints that do not require authentication, such as authentication APIs,
     *       shared file access, download, view, comment endpoints, and PDF search.</li>
     *   <li>Requiring authentication for all other endpoints.</li>
     *   <li>Allowing frames from the same origin (useful for H2 console or similar tools).</li>
     *   <li>Customizing the response for unauthorized access attempts with a JSON error message and 403 status.</li>
     *   <li>Enabling CORS with a custom configuration source.</li>
     *   <li>Adding a JWT authentication filter before the standard username/password authentication filter.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to modify
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/api/auth/**",
        "/api/shared/access/**",
        "/api/shared/download/**",
        "/api/shared/view/**",
        "/api/shared/*/comments",   // <-- New public endpoint for adding comments to shared files
        "/api/pdf/search"
    ).permitAll()
    .anyRequest().authenticated()
)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow framing from same origin
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Forbidden - Not Authorized\"}");
                })
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Add this line for CORS
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates and configures a {@link DaoAuthenticationProvider} bean.
     * <p>
     * This provider uses the injected {@code userDetailsService} to retrieve user details
     * and the configured password encoder for password verification.
     * </p>
     *
     * @return a fully configured {@link DaoAuthenticationProvider} instance
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Defines an {@link AuthenticationManager} bean for the application.
     * <p>
     * This method retrieves the {@link AuthenticationManager} from the provided
     * {@link AuthenticationConfiguration}. The {@code AuthenticationManager} is a
     * core component of Spring Security that is responsible for processing authentication
     * requests.
     *
     * @param authConfig the {@link AuthenticationConfiguration} used to obtain the {@link AuthenticationManager}
     * @return the configured {@link AuthenticationManager} instance
     * @throws Exception if an error occurs while retrieving the {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Creates and configures a {@link CorsConfigurationSource} bean for handling CORS (Cross-Origin Resource Sharing) requests.
     * <p>
     * This configuration allows requests from specific frontend origins, permits common HTTP methods,
     * and allows certain headers such as Authorization, Cache-Control, and Content-Type.
     * Credentials are allowed in cross-origin requests.
     * The CORS configuration is applied to all endpoints matching the "/api/**" path pattern.
     *
     * @return a configured {@link CorsConfigurationSource} instance for use in Spring Security.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174", "http://65.2.32.133:3000")); // Allow your frontend origins
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Apply CORS to /api/** paths
        return source;
    }
}
