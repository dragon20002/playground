package net.ldcc.playground.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

public class AuthInterceptorAdapter extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptorAdapter.class);

    /**
     * Interceptor 통과 및 다음 Chain 실행 여부 로직수행
     *
     * @return Intercepter 통과 여부. true면 다음 Chain을 실행한다.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        Enumeration<String> names = session.getAttributeNames();
        logger.debug("userId={}", userId);
        while (names.hasMoreElements()) {
            logger.debug("preHandle session : {}", names.nextElement());
        }

        if (userId == null) {
            String method = request.getMethod();
            String uri = request.getRequestURI(); // ex) uri=/api/members, url=http://localhost:8080/api/members

            // API 요청인 경우 상태코드 403으로 응답
            if (uri.matches("/api/members.*")) {
                switch (method.toUpperCase()) {
                    case "OPTIONS":
                    case "POST":
                        return true;
                    default:
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
                return true;

            } else if (uri.matches("/api/.*")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return true;

            } else { // 아니면 로그인화면으로 이동
                response.sendRedirect(request.getContextPath() + "/login");
            }

            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }
}
