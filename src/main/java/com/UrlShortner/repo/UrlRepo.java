package com.UrlShortner.repo;

import com.UrlShortner.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepo extends JpaRepository< Url,Long> {


    @Modifying
    @Query("""
        UPDATE Url u
        SET u.clickCount = u.clickCount + :count
        WHERE u.id = :id
        """)
    int addClickCount(
            @Param("id") Long id,
            @Param("count") long count
    );
}
