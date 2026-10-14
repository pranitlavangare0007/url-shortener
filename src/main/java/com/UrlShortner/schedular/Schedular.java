package com.UrlShortner.schedular;

import com.UrlShortner.repo.UrlRepo;
import io.seruco.encoding.base62.Base62;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class Schedular {


    private static final Logger log = LoggerFactory.getLogger(Schedular.class);
    final StringRedisTemplate redisTemplate;
    final Base62 base62;
    final UrlRepo urlRepo;

    public Schedular(StringRedisTemplate redisTemplate,Base62 base62,UrlRepo urlRepo){
        this.urlRepo=urlRepo;
        this.base62=base62;
        this.redisTemplate=redisTemplate;
    }
    @Scheduled(fixedRate = 30000)
    public void updateCount() {
        System.out.println("schedular called");

        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match("clicks:*").count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) keys.add(cursor.next());
        }

        for (String key : keys) {
            System.out.println("count updated");
            // 1. decode first: a bad key must not loop forever
            long id;
            try {
                String shortUrl = key.substring("clicks:".length());
                id = Long.parseLong(new String(base62.decode(shortUrl.getBytes(StandardCharsets.UTF_8))));
            } catch (Exception e) {
                log.warn("Bad key {}, deleting it", key);
                redisTemplate.delete(key);
                continue;
            }

            // 2. take the count out of Redis atomically
            String value = redisTemplate.opsForValue().getAndDelete(key);
            if (value == null) continue;
            long count = Long.parseLong(value);

            // 3. save to DB; on failure, give the clicks back to Redis
            try {
                int rows = urlRepo.addClickCount(id, count);
                if (rows == 0) {
                    log.warn("No row for {}, dropping {} clicks", key, count);
                } else {
                    log.info("Flushed {} clicks for id {} ({})", count, id, key);
                }
            } catch (Exception e) {
                log.error("Failed to flush {}, restoring {} clicks", key, count, e);
                redisTemplate.opsForValue().increment(key, count);
            }
        }
    }

}
