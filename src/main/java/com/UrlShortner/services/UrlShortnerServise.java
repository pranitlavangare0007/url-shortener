package com.UrlShortner.services;

import com.UrlShortner.model.Url;
import com.UrlShortner.repo.UrlRepo;
import io.seruco.encoding.base62.Base62;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;


@Service
public class UrlShortnerServise {

    final Base62 base62 ;
    final UrlRepo urlRepo;


    public UrlShortnerServise(Base62 base62 ,UrlRepo urlRepo){
        this.base62=base62;
        this.urlRepo=urlRepo;
    }

    public String shortUrl(String url) {

        Url url1 = new Url();
        url1.setUrl(url);
        Url savedUrl = urlRepo.save(url1);

      byte[] encBytes =  base62.encode(Long.toString(savedUrl.getId()).getBytes(StandardCharsets.UTF_8));


        return "http://localhost:8080/" + new String(encBytes);
    }
}
