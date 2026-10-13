package com.UrlShortner.interceptors;


import com.UrlShortner.services.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService){
        this.rateLimiterService=rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request , HttpServletResponse response , Object handler) throws Exception{

        String clintIp = request.getRemoteAddr();

        if(!rateLimiterService.isAllowed(clintIp)){
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests. Try again later.\"}");
            return false;
        }

        return true;
    }
}
