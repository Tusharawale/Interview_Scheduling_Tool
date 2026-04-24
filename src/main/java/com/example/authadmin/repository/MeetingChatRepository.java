package com.example.authadmin.repository;

import com.example.authadmin.entity.MeetingChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetingChatRepository extends JpaRepository<MeetingChatMessage, Long> {

    List<MeetingChatMessage> findByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MeetingChatMessage")
    void deleteAllMessages();
}
