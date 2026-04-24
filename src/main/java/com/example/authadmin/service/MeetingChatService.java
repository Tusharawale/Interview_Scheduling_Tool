package com.example.authadmin.service;

import com.example.authadmin.dto.MeetingDtos;
import com.example.authadmin.entity.MeetingChatMessage;
import com.example.authadmin.repository.MeetingChatRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingChatService {

    private final MeetingChatRepository repository;

    public MeetingChatService(MeetingChatRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveFromEnvelope(MeetingDtos.WsEnvelope message) {
        if (message == null) return;
        MeetingDtos.ChatPayload p = toPayload(message.getPayload());
        if (p == null) return;
        boolean hasText = p.getText() != null && !p.getText().isBlank();
        boolean hasFile = p.getFileUrl() != null && !p.getFileUrl().isBlank();
        if (!hasText && !hasFile) return;

        MeetingChatMessage m = new MeetingChatMessage();
        m.setSenderRole(message.getSender());
        m.setSenderName(message.getSenderName());
        m.setSenderUserId(message.getSenderId());
        m.setFromClientId(message.getFromClientId());
        if (hasText) m.setMessageText(p.getText().trim());
        if (hasFile) {
            m.setFileName(p.getFileName());
            m.setFileUrl(p.getFileUrl());
        }
        repository.save(m);
    }

    private static MeetingDtos.ChatPayload toPayload(Object raw) {
        if (raw instanceof MeetingDtos.ChatPayload p) return p;
        if (raw instanceof java.util.Map<?, ?> map) {
            MeetingDtos.ChatPayload p = new MeetingDtos.ChatPayload();
            Object t = map.get("text");
            Object fn = map.get("fileName");
            Object fu = map.get("fileUrl");
            if (t != null) p.setText(String.valueOf(t));
            if (fn != null) p.setFileName(String.valueOf(fn));
            if (fu != null) p.setFileUrl(String.valueOf(fu));
            return p;
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<MeetingDtos.ChatHistoryItem> recent(int limit) {
        int n = Math.min(Math.max(limit, 1), 200);
        List<MeetingChatMessage> rows = repository.findByOrderByCreatedAtDesc(PageRequest.of(0, n));
        Collections.reverse(rows);
        return rows.stream().map(this::toItem).collect(Collectors.toList());
    }

    private MeetingDtos.ChatHistoryItem toItem(MeetingChatMessage m) {
        MeetingDtos.ChatHistoryItem i = new MeetingDtos.ChatHistoryItem();
        i.setSenderRole(m.getSenderRole());
        i.setSenderName(m.getSenderName());
        i.setSenderUserId(m.getSenderUserId());
        i.setFromClientId(m.getFromClientId());
        i.setText(m.getMessageText());
        i.setFileName(m.getFileName());
        i.setFileUrl(m.getFileUrl());
        i.setTs(m.getCreatedAt());
        return i;
    }

    /** Clears persisted live chat when the host ends the meeting so the next session starts empty. */
    @Transactional
    public void clearAllMessages() {
        repository.deleteAllMessages();
    }
}
