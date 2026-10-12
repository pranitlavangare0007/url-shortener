package com.UrlShortner.controller;

import com.UrlShortner.Dto.UrlReq;
import com.UrlShortner.services.UrlClickTrackingService;
import com.UrlShortner.services.UrlShortnerServise;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {


    final UrlShortnerServise urlShortnerServise;
    final UrlClickTrackingService urlClickTrackingService;

    public UrlController(UrlShortnerServise urlShortnerServise ,UrlClickTrackingService urlClickTrackingService) {
        this.urlShortnerServise = urlShortnerServise;
        this.urlClickTrackingService=urlClickTrackingService;

    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> tempRedirect(@PathVariable String shortUrl) {

        String originalUrl = urlShortnerServise.isUrlPresent(shortUrl);
        urlClickTrackingService.trackCount(shortUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);

    }

    @PostMapping("/url")
    public String urlShortname(@RequestBody UrlReq urlReq) {

        return urlShortnerServise.shortUrl(urlReq.url());
    }

}
