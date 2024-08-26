package com.smartContact.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(); // Ensure this class is properly defined
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure the DaoAuthenticationProvider with the UserDetailsService and PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expose the AuthenticationManager bean that delegates authentication to the configured AuthenticationProvider.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configure the SecurityFilterChain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/admin/**").hasRole("ADMIN") // Restrict '/admin/' URLs to ADMIN role
                .requestMatchers("/user/**").hasRole("USER")   // Restrict '/user/' URLs to USER role
                .requestMatchers("/**").permitAll()            // Permit all other URLs
                .anyRequest().authenticated()                  // All other URLs require authentication
            )
            .formLogin(form -> form
                    .loginPage("/signin")                          // Specify custom login page
                    .loginProcessingUrl("/dologin")
                    .defaultSuccessUrl("/user/index")
                    .permitAll()
                )
                .logout(withDefaults())       // Configure logout with default settings
                .csrf().disable()             // Disable CSRF for simplicity
                .authenticationProvider(authenticationProvider()); // Configure the authentication provider

            return http.build();
        }
    }