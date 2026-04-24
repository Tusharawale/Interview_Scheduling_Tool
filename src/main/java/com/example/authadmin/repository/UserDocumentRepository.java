package com.example.authadmin.repository;

import com.example.authadmin.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Integer> {
    List<UserDocument> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
}
