package com.shivangee.jobportal.service;

import com.shivangee.jobportal.dto.ApplicationResponse;
import com.shivangee.jobportal.dto.ApplyRequest;
import com.shivangee.jobportal.dto.PageResponse;
import com.shivangee.jobportal.entity.ApplicationStatus;
import com.shivangee.jobportal.entity.Job;
import com.shivangee.jobportal.entity.JobApplication;
import com.shivangee.jobportal.entity.User;
import com.shivangee.jobportal.exception.ConflictException;
import com.shivangee.jobportal.exception.ForbiddenException;
import com.shivangee.jobportal.exception.ResourceNotFoundException;
import com.shivangee.jobportal.repository.JobApplicationRepository;
import com.shivangee.jobportal.repository.JobRepository;
import com.shivangee.jobportal.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApplicationService(JobApplicationRepository applicationRepository,
                              JobRepository jobRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public ApplicationResponse apply(Long jobId, ApplyRequest req, String email) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));
        if (!job.isActive()) {
            throw new ResourceNotFoundException("Job " + jobId + " is closed");
        }
        User candidate = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ConflictException("You have already applied to this job");
        }
        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setCoverLetter(req == null ? null : req.coverLetter());
        application.setStatus(ApplicationStatus.APPLIED);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> myApplications(String email, int page, int size) {
        User candidate = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return PageResponse.from(applicationRepository
                .findByCandidateId(candidate.getId(), pageable(page, size))
                .map(ApplicationResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> applicationsForJob(Long jobId, String email, int page, int size) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));
        if (!job.getPostedBy().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You can only view applications for your own jobs");
        }
        return PageResponse.from(applicationRepository
                .findByJobId(jobId, pageable(page, size))
                .map(ApplicationResponse::from));
    }

    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status, String email) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + applicationId));
        if (!application.getJob().getPostedBy().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You can only update applications for your own jobs");
        }
        application.setStatus(status);
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "appliedAt"));
    }
}
