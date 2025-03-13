package com.ifpe.techifolio.config;

import com.ifpe.techifolio.service.CustomOAuth2UserService;
import com.ifpe.techifolio.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeHttpRequests ->
                authorizeHttpRequests
                    .requestMatchers("/", "/login", "/login**", "/error**", "/oauth2/**", "/logout", "/alunos/login", "/alunos/recuperar-senha", "/alunos").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .loginProcessingUrl("/alunos/login")
                    .defaultSuccessUrl("/home", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
            )
            .oauth2Login(oauth2Login ->
                oauth2Login
                    .loginPage("/login")
                    .defaultSuccessUrl("/home", true)
                    .userInfoEndpoint(userInfoEndpoint ->
                        userInfoEndpoint
                            .userService(customOAuth2UserService)
                    )
            )
            .logout(logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/alunos/login", "/alunos/recuperar-senha", "/logout")
            )
            .sessionManagement(sessionManagement ->
                sessionManagement
                    .sessionFixation().newSession()
            );
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
}