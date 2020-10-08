package net.ldcc.playground.config_bak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class BaseWebMvcConfigurer implements WebMvcConfigurer {
//    @Autowired
    private WebApplicationContext context;

    /**
     * CORS 허용할 도메인들을 추가한다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "DELETE")
                .allowedOrigins("http://localhost:8080",
                        "http://localhost:8081");
    }

    /**
     * 권한에 대한 Interceptor Bean 생성
     */
    @Bean(name = "authInterceptor")
    public AuthInterceptorAdapter getAuthInterceptorAdapter() {
        return new AuthInterceptorAdapter();
    }

    /**
     * CORS에 대한 Interceptor Bean 생성
     */
    @Bean(name = "corsInterceptor")
    public CorsInterceptorAdapter getCorsInterceptorAdapter() {
        return new CorsInterceptorAdapter();
    }

    /**
     * Interceptor들을 추가한다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        CorsInterceptorAdapter corsInterceptorAdapter = (CorsInterceptorAdapter) context.getBean("corsInterceptor");
        AuthInterceptorAdapter authInterceptorAdapter = (AuthInterceptorAdapter) context.getBean("authInterceptor");

        // CORS Interceptor에 대한 White-List를 추가한다.
//        registry.addInterceptor(corsInterceptorAdapter)
//                .addPathPatterns("/**");

        // 요청 권한 Interceptor에 대한 White-List를 추가한다.
        registry.addInterceptor(authInterceptorAdapter)
                .excludePathPatterns("/", "/login", "/error");
    }

}
