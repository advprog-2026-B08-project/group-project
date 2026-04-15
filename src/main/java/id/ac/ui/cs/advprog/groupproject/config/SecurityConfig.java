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
                        .requestMatchers("/login", "/register", "/h2-console/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/catalog/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_JASTIPER")
                        .requestMatchers("/kycRequestJastiper/**").hasAuthority("ROLE_TITIPER")
                        .requestMatchers("/kycRequestAdmin/**").hasAuthority("ROLE_JASTIPER")
                        .anyRequest().authenticated()
        ).formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/homepage", true)
                .permitAll()
        ).logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        ).csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
        ).headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
        ).exceptionHandling(error -> error
                .accessDeniedHandler((request, response, exception) -> {
                    request.getSession().setAttribute("unauthorized", true);
                    response.sendRedirect("/homepage");
                })
        );

        return http.build();
    }
}
