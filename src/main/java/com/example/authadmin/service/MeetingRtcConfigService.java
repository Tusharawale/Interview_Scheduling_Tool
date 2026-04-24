package com.example.authadmin.service;

import com.example.authadmin.dto.MeetingDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MeetingRtcConfigService {

    @Value("${app.meeting.ice.stun-urls:stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302}")
    private String stunUrls;

    @Value("${app.meeting.turn.urls:}")
    private String turnUrls;

    @Value("${app.meeting.turn.username:}")
    private String turnUsername;

    @Value("${app.meeting.turn.password:}")
    private String turnPassword;

    @Value("${app.meeting.topology:mesh}")
    private String topology;

    public MeetingDtos.RtcConfigResponse build() {
        List<Map<String, Object>> iceServers = new ArrayList<>();
        for (String u : splitCsv(stunUrls)) {
            iceServers.add(Map.of("urls", u));
        }
        for (String u : splitCsv(turnUrls)) {
            Map<String, Object> turn = new LinkedHashMap<>();
            turn.put("urls", u);
            if (turnUsername != null && !turnUsername.isBlank()) {
                turn.put("username", turnUsername.trim());
                turn.put("credential", turnPassword != null ? turnPassword : "");
            }
            iceServers.add(turn);
        }
        MeetingDtos.RtcConfigResponse r = new MeetingDtos.RtcConfigResponse();
        r.setIceServers(iceServers);
        r.setMode(topology != null ? topology : "mesh");
        return r;
    }

    private static List<String> splitCsv(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null || raw.isBlank()) return out;
        for (String p : raw.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }
}
