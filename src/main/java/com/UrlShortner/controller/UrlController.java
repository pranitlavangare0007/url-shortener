package com.UrlShortner.controller;

import com.UrlShortner.Dto.UrlReq;
import com.UrlShortner.services.UrlShortnerServise;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {

    final UrlShortnerServise urlShortnerServise;

    public UrlController(UrlShortnerServise urlShortnerServise){
        this.urlShortnerServise=urlShortnerServise;
    }

    @PostMapping("/url")
    public String urlShortner(@RequestBody UrlReq urlReq){
        System.out.println(urlReq.url());
        return urlShortnerServise.shortUrl(urlReq.url());
    }
}
