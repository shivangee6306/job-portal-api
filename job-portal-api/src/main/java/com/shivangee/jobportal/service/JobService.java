package com.shivangee.jobportal.service;

import com.shivangee.jobportal.dto.JobRequest;
import com.shivangee.jobportal.dto.JobResponse;
import com.shivangee.jobportal.dto.PageResponse;
import com.shivangee.jobportal.entity.Job;
import com.shivangee.jobportal.entity.Role;
import com.shivangee.jobportal.entity.User;
import com.shivangee.jobportal.exception.ForbiddenException;
import com.shivangee.jobportal.exception.ResourceNotFoundException;
import com.shivangee.jobportal.repository.JobRepository;
import com.shivangee.jobportal.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class JobService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "salary", "title", "company");

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public JobResponse create(JobRequest req, String email) {
        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Job job = new Job();
        apply(job, req);
        job.setPostedBy(recruiter);
        return JobResponse.from(jobRepository.save(job));
    }

    public JobResponse update(Long id, JobRequest req, String email) {
        Job job = findJob(id);
        if (!job.getPostedBy().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You can only edit your own jobs");
        }
        apply(job, req);
        return JobResponse.from(jobRepository.save(job));
    }

    /** Soft delete: job is closed, applications stay in the database. */
    public void close(Long id, String email) {
        Job job = findJob(id);
        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isOwner = job.getPostedBy().getId().equals(caller.getId());
        if (!isOwner && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only close your own jobs");
        }
        job.setActive(false);
        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public JobResponse getById(Long id) {
        Job job = findJob(id);
        if (!job.isActive()) {
            throw new ResourceNotFoundException("Job " + id + " is closed");
        }
        return JobResponse.from(job);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> search(String keyword, String location,
                                            int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<JobResponse> result = jobRepository
                .search(keyword == null ? "" : keyword.trim(), location == null ? "" : location.trim(), pageable)
                .map(JobResponse::from);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> myJobs(String email, int page, int size) {
        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Pageable pageable = buildPageable(page, size, "createdAt", "desc");
        return PageResponse.from(jobRepository.findByPostedById(recruiter.getId(), pageable).map(JobResponse::from));
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));
    }

    private void apply(Job job, JobRequest req) {
        job.setTitle(req.title().trim());
        job.setDescription(req.description().trim());
        job.setCompany(req.company().trim());
        job.setLocation(req.location().trim());
        job.setSalary(req.salary());
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String field = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(dir, field));
    }
}
