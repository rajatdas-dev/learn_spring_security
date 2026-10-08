package com.example.spring_security_demo.repository;

import com.example.spring_security_demo.entity.UserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, Long> {
    
    Optional<UserIdentityEntity> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );
    
    boolean existsByProviderAndProviderSubject(
            String subject,
            String providerSubject
    );
}
