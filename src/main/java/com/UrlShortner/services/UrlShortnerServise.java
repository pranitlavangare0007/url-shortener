package com.UrlShortner.services;

import com.UrlShortner.exceptions.InvalidUrlException;
import com.UrlShortner.model.Url;
import com.UrlShortner.repo.UrlRepo;
import io.seruco.encoding.base62.Base62;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
public class UrlShortnerServise {
    final StringRedisTemplate redisTemplate;
    final Base62 base62;
    final UrlRepo urlRepo;


    public UrlShortnerServise(Base62 base62, UrlRepo urlRepo, StringRedisTemplate redisTemplate) {
        this.base62 = base62;
        this.urlRepo = urlRepo;
        this.redisTemplate = redisTemplate;
    }

    public String shortUrl(String url) {

        Url url1 = new Url();
        url1.setUrl(url);
        Url savedUrl = urlRepo.save(url1);

        byte[] encBytes = base62.encode(Long.toString(savedUrl.getId()).getBytes(StandardCharsets.UTF_8));

        return "http://localhost:8080/" + new String(encBytes);
    }

    public String isUrlPresent(String url) {


        try {
            String key = "url:" + url;
            String cashedUrl = redisTemplate.opsForValue().get(key);
            if (cashedUrl != null) {

                return cashedUrl;
            }

            Long originalIdx = Long.parseLong(new String(base62.decode(url.getBytes(StandardCharsets.UTF_8))));

            Url originalUrl = urlRepo.findById(originalIdx)
                    .orElseThrow(() -> new InvalidUrlException("Please enter valid url"));

            redisTemplate.opsForValue().set(key, originalUrl.getUrl(), Duration.ofMinutes(15));

            return originalUrl.getUrl();
        } catch (NumberFormatException ex) {
            throw new InvalidUrlException("Please enter valid url");
        }


    }
}
