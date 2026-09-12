package com.myga.learning.backend.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Stateless JWT security. Authentication is required for every /api endpoint
 * except login. Writes are restricted to ADMIN for now; fine-grained
 * ownership rules (parent -> own children, teacher -> assignments) are added
 * as those relationships are introduced.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                    // Specific first: changing your own password needs a valid
                    // session, even though the rest of /api/auth is public.
                    .antMatchers(HttpMethod.POST, "/api/auth/change-password").authenticated()
                    .antMatchers("/api/auth/**").permitAll()
                    .antMatchers("/h2-console/**").permitAll()
                    // API documentation.
                    .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    // Role-specific portals.
                    .antMatchers("/api/parent/**").hasRole("PARENT")
                    .antMatchers("/api/teacher/**").hasRole("TEACHER")
                    // Grades and attendance may be recorded by an admin or a
                    // teacher; the service enforces the teacher's own-assignment
                    // ownership.
                    .antMatchers(HttpMethod.POST, "/api/grades").hasAnyRole("ADMIN", "TEACHER")
                    // Editing/deleting is further restricted in the service to
                    // the teacher who recorded the grade.
                    .antMatchers(HttpMethod.PUT, "/api/grades/**").hasAnyRole("ADMIN", "TEACHER")
                    .antMatchers(HttpMethod.DELETE, "/api/grades/**").hasAnyRole("ADMIN", "TEACHER")
                    .antMatchers(HttpMethod.POST, "/api/attendance", "/api/attendance/**").hasAnyRole("ADMIN", "TEACHER")
                    .antMatchers(HttpMethod.POST, "/api/observations").hasAnyRole("ADMIN", "TEACHER")
                    .antMatchers(HttpMethod.POST, "/api/assessments").hasAnyRole("ADMIN", "TEACHER")
                    // Every user manages their own notifications (ownership in service).
                    .antMatchers(HttpMethod.PUT, "/api/notifications/**").hasAnyRole("ADMIN", "TEACHER", "PARENT")
                    // The full announcement list is an admin management view; /me is open to all.
                    .antMatchers(HttpMethod.GET, "/api/announcements").hasRole("ADMIN")
                    // Account administration is admin-only, reads included.
                    .antMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
                    // Each dashboard is restricted to its own role.
                    .antMatchers(HttpMethod.GET, "/api/dashboard/admin").hasRole("ADMIN")
                    .antMatchers(HttpMethod.GET, "/api/dashboard/teacher").hasRole("TEACHER")
                    .antMatchers(HttpMethod.GET, "/api/dashboard/parent").hasRole("PARENT")
                    // Reads are open to any authenticated role; ownership on
                    // sensitive reads (e.g. a student's grades) is enforced in
                    // the service layer, not by the URL alone.
                    .antMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "TEACHER", "PARENT")
                    // All other writes are administrator-only.
                    .antMatchers("/api/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Allow the H2 console to render inside a frame (development only).
        http.headers().frameOptions().disable();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Angular dev server; adjust for production origins.
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
