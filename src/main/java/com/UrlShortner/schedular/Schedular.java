package com.UrlShortner.schedular;

import com.UrlShortner.repo.UrlRepo;
import io.seruco.encoding.base62.Base62;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.util.Set;


public class Schedular {

    final StringRedisTemplate redisTemplate;
    final Base62 base62;
    final UrlRepo urlRepo;

    public Schedular(StringRedisTemplate redisTemplate,Base62 base62,UrlRepo urlRepo){
        this.urlRepo=urlRepo;
        this.base62=base62;
        this.redisTemplate=redisTemplate;
    }
    @Scheduled(fixedRate = 30000)
    public void updateCount(){
        Set<String> keys = redisTemplate.keys("clicks:*");

        if(keys == null || keys.isEmpty()){
            return;
        }

        for(String key : keys){

            String shortUrl=key.substring("clicks:".length());
            Long id = Long.parseLong(new String(base62.decode(shortUrl.getBytes(StandardCharsets.UTF_8))));
            String countValue = redisTemplate.opsForValue().get(key);
            if(countValue == null){
                continue;
            }
            long count =Long.parseLong(countValue);
            if(count <= 0){
                continue;
            }

            int updated = urlRepo.addClickCount(id,count);

            if(updated > 0){
                redisTemplate.delete(key);
            }
        }

    }
}
