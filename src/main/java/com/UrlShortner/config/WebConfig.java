package com.UrlShortner.config;

import com.UrlShortner.interceptors.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(RateLimitInterceptor interceptor){
        this.rateLimitInterceptor=interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/url");
    }
}
