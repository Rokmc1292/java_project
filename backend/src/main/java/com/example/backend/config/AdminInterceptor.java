// backend/src/main/java/com/example/backend/config/AdminInterceptor.java
package com.example.backend.config;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 요청은 통과
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"로그인이 필요합니다.\"}");
            return false;
        }

        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"로그인이 필요합니다.\"}");
            return false;
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getIsAdmin())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"관리자 권한이 필요합니다.\"}");
            return false;
        }

        return true;
    }
}