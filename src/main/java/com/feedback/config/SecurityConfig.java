package com.feedback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Admin area
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Feedback mutations (must come BEFORE general /feedback/** permit)
                        .requestMatchers(HttpMethod.GET, "/feedback/new").authenticated()
                        .requestMatchers(HttpMethod.GET, "/feedback/*/edit").authenticated()
                        .requestMatchers(HttpMethod.POST, "/feedback").authenticated()
                        .requestMatchers(HttpMethod.POST, "/feedback/*").authenticated() // update
                        .requestMatchers(HttpMethod.POST, "/feedback/*/delete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/feedback/*/upvote").authenticated()

                        // Comment mutations
                        .requestMatchers(HttpMethod.POST, "/feedback/*/comments").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comments/*/edit").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comments/*/delete").authenticated()

                        // Reply mutations
                        .requestMatchers(HttpMethod.POST, "/comments/*/replies").authenticated()
                        .requestMatchers(HttpMethod.POST, "/replies/*/edit").authenticated()
                        .requestMatchers(HttpMethod.POST, "/replies/*/delete").authenticated()

                        // Profile routes
                        .requestMatchers("/profile/**").authenticated()

                        // Public routes and assets
                        .requestMatchers("/", "/feedback/**", "/roadmap", "/auth/**",
                                         "/css/**", "/js/**", "/img/**").permitAll()

                        // Any other request needs authentication by default
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // Forward to our Thymeleaf 403 page when access is denied
                        .accessDeniedPage("/error/403")
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                // Use sensible defaults for the rest
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
