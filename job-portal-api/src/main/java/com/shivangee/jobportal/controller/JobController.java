package com.shivangee.jobportal.controller;

import com.shivangee.jobportal.dto.JobRequest;
import com.shivangee.jobportal.dto.JobResponse;
import com.shivangee.jobportal.dto.PageResponse;
import com.shivangee.jobportal.service.JobService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** Public: search + pagination + sorting. */
    @GetMapping
    public PageResponse<JobResponse> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return jobService.search(keyword, location, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public JobResponse getById(@PathVariable Long id) {
        return jobService.getById(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public PageResponse<JobResponse> myJobs(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return jobService.myJobs(principal.getUsername(), page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> create(
            @Valid @RequestBody JobRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.create(request, principal.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse update(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
        return jobService.update(id, request, principal.getUsername());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<Void> close(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
        jobService.close(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
