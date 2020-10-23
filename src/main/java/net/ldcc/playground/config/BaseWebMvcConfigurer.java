package net.ldcc.playground.config;

import net.ldcc.playground.config.auth_bak.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.ldcc.playground.util.JwtTokenProvider;

@Configuration
public class BaseWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private WebApplicationContext context;

    /**
     * 권한에 대한 Interceptor Bean 생성
     */
    @Bean
    public AuthInterceptor authInterceptor(JwtTokenProvider jwtTokenProvider) {
        return new AuthInterceptor(jwtTokenProvider);
    }

    /**
     * Interceptor를 추가한다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthInterceptor authInterceptorAdapter = (AuthInterceptor) context.getBean("authInterceptor");

        registry.addInterceptor(authInterceptorAdapter)
        		.addPathPatterns("/api/members/**");
    }

}
