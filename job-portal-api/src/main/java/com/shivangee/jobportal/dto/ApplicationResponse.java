package com.shivangee.jobportal.dto;

import com.shivangee.jobportal.entity.ApplicationStatus;
import com.shivangee.jobportal.entity.JobApplication;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long candidateId,
        String candidateName,
        String candidateEmail,
        String coverLetter,
        ApplicationStatus status,
        LocalDateTime appliedAt) {

    public static ApplicationResponse from(JobApplication a) {
        return new ApplicationResponse(
                a.getId(), a.getJob().getId(), a.getJob().getTitle(),
                a.getCandidate().getId(), a.getCandidate().getName(), a.getCandidate().getEmail(),
                a.getCoverLetter(), a.getStatus(), a.getAppliedAt());
    }
}
