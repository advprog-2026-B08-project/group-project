package id.ac.ui.cs.advprog.groupproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration @EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/login", "/register", "/h2-console/**")
                        .permitAll().anyRequest().authenticated()
        ).formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/homepage", true)
                .permitAll()
        ).logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
        ).csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
        ).headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
        );

        return http.build();
    }
}
