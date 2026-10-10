package com.UrlShortner.repo;

import com.UrlShortner.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepo extends JpaRepository< Url,Long> {

Optional<Url> findById(Long id );
}
