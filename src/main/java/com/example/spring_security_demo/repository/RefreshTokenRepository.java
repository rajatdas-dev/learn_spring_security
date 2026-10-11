package com.example.spring_security_demo.repository;

import com.example.spring_security_demo.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select token
        from RefreshTokenEntity token
        where token.tokenHash = :tokenHash
        """)
    Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying(flushAutomatically = true)
    @Query("""
        update RefreshTokenEntity token
        set token.revokedAt = :revokedAt
        where token.familyId = :familyId
          and token.revokedAt is null
        """)
    int revokeActiveFamily(
            @Param("familyId") String familyId,
            @Param("revokedAt")Instant revokedAt
            );
}
