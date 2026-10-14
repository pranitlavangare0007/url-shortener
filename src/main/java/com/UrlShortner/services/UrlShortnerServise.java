package com.UrlShortner.services;

import com.UrlShortner.exceptions.InvalidInputException;
import com.UrlShortner.exceptions.InvalidUrlException;
import com.UrlShortner.model.Url;
import com.UrlShortner.repo.UrlRepo;
import io.seruco.encoding.base62.Base62;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;



@Service
public class UrlShortnerServise {
    final StringRedisTemplate redisTemplate;
    final Base62 base62;
    final UrlRepo urlRepo;

    private void validate(String url) {
        if (url == null || url.isBlank() || url.length() > 2048)
            throw new InvalidInputException("URL is required (max 2048 characters)");
        try {
            java.net.URI uri = new java.net.URI(url.trim());
            String scheme = uri.getScheme();
            boolean ok = scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && uri.getHost() != null;
            if (!ok) throw new InvalidInputException("Only valid http/https URLs are allowed");
        } catch (java.net.URISyntaxException e) {
            throw new InvalidInputException("Malformed URL");
        }
    }
    public UrlShortnerServise(Base62 base62, UrlRepo urlRepo, StringRedisTemplate redisTemplate) {
        this.base62 = base62;
        this.urlRepo = urlRepo;
        this.redisTemplate = redisTemplate;
    }

    public String shortUrl(String url) {

        validate(url);
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
