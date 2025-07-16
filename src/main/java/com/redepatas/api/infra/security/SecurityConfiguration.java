package com.redepatas.api.infra.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()
                        // .requestMatchers(HttpMethod.POST, "/pets/newPet/{userId}").hasRole("USER")
                        // .requestMatchers(HttpMethod.GET, "/pets/petsByClient/").hasRole("USER")
                        // .requestMatchers(HttpMethod.DELETE, "/pets/deletePet/{id}").hasRole("USER")
                        // .requestMatchers(HttpMethod.PUT, "/pets/updatePet/{id}").hasRole("USER")
                        // .requestMatchers(HttpMethod.POST, "/pets/addVacina/{petId}").hasRole("USER")
                        // .requestMatchers(HttpMethod.DELETE, "/pets/deleteVacina/").hasRole("USER")

                        // .requestMatchers(HttpMethod.PUT, "/users/updateProfile").hasRole("USER")
                        // .requestMatchers(HttpMethod.PUT, "/auth/changePassword").hasRole("USER")
                        // .requestMatchers(HttpMethod.PUT, "/users/changeAvatar").hasRole("USER")
                        // .requestMatchers(HttpMethod.GET, "/users/getUser").hasRole("USER")

                        // .requestMatchers(HttpMethod.GET, "/users/myAddress").hasRole("USER")
                        // .requestMatchers(HttpMethod.POST, "/users/newAddress").hasRole("USER")
                        // .requestMatchers(HttpMethod.PUT,
                        // "/users/updateAddress/{idAddress}").hasRole("USER")
                        // .requestMatchers(HttpMethod.DELETE,
                        // "/users/deleteAddress/{idAddress}").hasRole("USER")
                        // .requestMatchers(HttpMethod.PUT,
                        // "/users/selectAddress/{idAddress}").hasRole("USER")

                        // .requestMatchers(HttpMethod.POST,
                        // "/agendamentos/criarAgendamento").hasRole("USER")
                        // .requestMatchers(HttpMethod.PUT, "/agendamentos/avaliar").hasRole("USER")
                        // .requestMatchers(HttpMethod.GET,
                        // "/agendamentos/meusAgendamentos").hasRole("USER")
                        // .requestMatchers(HttpMethod.PUT,
                        // "agendamentos/alterarStatus/{idAgendamento}/{status}")
                        // .permitAll()
                        // .requestMatchers("agendamentos/getAgendamento/{id}").permitAll()

                        .requestMatchers(HttpMethod.POST, "/assinaturas/newSignature/{login}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/assinaturas/getLink/{idSub}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/assinaturas/myPlan").hasRole("USER")
                        .requestMatchers("/assinaturas/**").permitAll()

                        // .requestMatchers(HttpMethod.POST, "/partners/getAll").hasRole("USER")
                        // .requestMatchers(HttpMethod.POST, "/partners/getServices")
                        // .hasRole("USER")
                        .requestMatchers("/partners/**").permitAll()

                        .requestMatchers("/files/**").permitAll() // AUTENTICAR EM BREVE
                        .requestMatchers("/webhook/**").permitAll() // ADICIONAR

                        .anyRequest().authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
