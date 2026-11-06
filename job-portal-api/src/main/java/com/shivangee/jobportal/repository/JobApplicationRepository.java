package com.shivangee.jobportal.repository;

import com.shivangee.jobportal.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);
    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);
}
