package net.ldcc.playground.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import net.ldcc.playground.config.auth.CustomOAuth2UserService;
import net.ldcc.playground.model.Role;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CustomOAuth2UserService oauth2UserService;
	
    /**
     * CORS 허용 Origin에 대한 White-List 추가
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Origins White-List
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://localhost:8081",
                "http://local.mm.net:8081"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 정적 자원 Security 설정 안함
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
                .frameOptions()
                .sameOrigin()
            .and()
                .httpBasic().disable()
                .formLogin().disable()
            .csrf()
                .ignoringAntMatchers("/api/**")
                .ignoringAntMatchers("/error/**")
                .ignoringAntMatchers("/h2-console/**")
            .and()
                .cors()
                .configurationSource(corsConfigurationSource())
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers("/api/members/**").hasRole(Role.ADMIN.name())
                .antMatchers("/api/login/get-user-info").hasAnyRole(Role.ADMIN.name(), Role.USER.name())
                .antMatchers("/", "/api/login/**").permitAll()
                .anyRequest().authenticated()
            .and()
            	.oauth2Login()
            		.userInfoEndpoint()
            			.userService(oauth2UserService)
        ;
    }

}
