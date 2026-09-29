package com.example.spring_security_demo.repository;

import com.example.spring_security_demo.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    
    List<DocumentEntity> findAllByOwnerUserId(Long ownerUserId);
}
