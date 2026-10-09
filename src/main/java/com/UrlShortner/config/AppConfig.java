package com.UrlShortner.config;

import io.seruco.encoding.base62.Base62;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
   public Base62 base62(){
        return Base62.createInstance();
    }
}
