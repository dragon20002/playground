package net.ldcc.playground.config;

import net.ldcc.playground.service.BaseUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final BaseUserDetailsService baseUserDetailsService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SecurityConfig(BaseUserDetailsService baseUserDetailsService) {
        this.baseUserDetailsService = baseUserDetailsService;
    }

    /**
     * CORS 허용 Origin에 대한 White-List 추가
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Origins White-List
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://localhost:8081"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
                .frameOptions()
                .sameOrigin()
            .and()
                .csrf()
                .ignoringAntMatchers("/api/**")
                .ignoringAntMatchers("/h2-console/**")
            .and()
                .cors()
            .and()
                .authorizeRequests()
                .antMatchers("/api/members/**").hasRole("USER")
                .antMatchers("/**").permitAll()
            .and()
                .formLogin();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(baseUserDetailsService)
            .passwordEncoder(passwordEncoder);
    }

}
