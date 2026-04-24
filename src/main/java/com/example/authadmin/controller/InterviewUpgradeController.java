package com.example.authadmin.controller;

import com.example.authadmin.dto.InterviewUpgradeDtos;
import com.example.authadmin.service.InterviewUpgradeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview-upgrade")
public class InterviewUpgradeController {
    private final InterviewUpgradeService interviewUpgradeService;

    public InterviewUpgradeController(InterviewUpgradeService interviewUpgradeService) {
        this.interviewUpgradeService = interviewUpgradeService;
    }

    @PostMapping("/communication/score")
    public InterviewUpgradeDtos.CommunicationScoreResponse scoreCommunication(@RequestBody InterviewUpgradeDtos.CommunicationScoreRequest request) {
        return interviewUpgradeService.scoreCommunication(request);
    }

    @GetMapping("/ranking/weights")
    public InterviewUpgradeDtos.RankingWeightResponse getWeights() {
        return interviewUpgradeService.getWeights();
    }

    @PutMapping("/ranking/weights")
    public InterviewUpgradeDtos.RankingWeightResponse updateWeights(@RequestBody InterviewUpgradeDtos.RankingWeightRequest req) {
        return interviewUpgradeService.updateWeights(req);
    }

    @GetMapping("/ranking")
    public InterviewUpgradeDtos.RankingResponse ranking() {
        return interviewUpgradeService.buildRanking();
    }

    @GetMapping("/report/final")
    public InterviewUpgradeDtos.FinalReportResponse finalReport() {
        return interviewUpgradeService.finalReport();
    }

    @PostMapping("/report/final/publish")
    public InterviewUpgradeDtos.PublishReportResponse publishFinalReport(@RequestParam(name = "sendEmails", defaultValue = "true") boolean sendEmails) {
        return interviewUpgradeService.publishFinalReport(sendEmails);
    }

    @GetMapping("/report/final/user/{userId}")
    public InterviewUpgradeDtos.UserPositionResponse latestUserPosition(@PathVariable("userId") Integer userId) {
        return interviewUpgradeService.latestUserPosition(userId);
    }

    @GetMapping("/coding/challenges")
    public List<InterviewUpgradeDtos.CodingChallengeResponse> listActiveChallenges() {
        return interviewUpgradeService.listActiveChallenges();
    }

    @GetMapping("/coding/challenges/all")
    public List<InterviewUpgradeDtos.CodingChallengeResponse> listAllChallenges() {
        return interviewUpgradeService.listAllChallenges();
    }

    @GetMapping("/coding/judge0/status")
    public Map<String, Object> judge0Status() {
        return interviewUpgradeService.judge0Status();
    }

    @PostMapping("/coding/challenges")
    public InterviewUpgradeDtos.CodingChallengeResponse createChallenge(@RequestBody InterviewUpgradeDtos.CodingChallengeRequest req) {
        return interviewUpgradeService.saveChallenge(req);
    }

    @PostMapping("/coding/submit")
    public InterviewUpgradeDtos.CodingSubmissionResponse submitCode(@RequestBody InterviewUpgradeDtos.CodingSubmissionRequest req) {
        return interviewUpgradeService.submitCode(req);
    }
}

