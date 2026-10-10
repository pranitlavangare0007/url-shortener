package com.UrlShortner.controller;

import com.UrlShortner.Dto.UrlReq;
import com.UrlShortner.services.UrlShortnerServise;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    final UrlShortnerServise urlShortnerServise;

    public UrlController(UrlShortnerServise urlShortnerServise){
        this.urlShortnerServise=urlShortnerServise;
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> tempRedirect(@PathVariable String shortUrl){
        System.out.println("reached");

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(urlShortnerServise.isUrlPresent(shortUrl)));
       return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT) ;

    }

    @PostMapping("/url")
    public String urlShortner(@RequestBody UrlReq urlReq){

        return urlShortnerServise.shortUrl(urlReq.url());
    }

}
