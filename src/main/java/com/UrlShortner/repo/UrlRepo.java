package com.UrlShortner.repo;

import com.UrlShortner.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepo extends JpaRepository< Url,Long> {
}
