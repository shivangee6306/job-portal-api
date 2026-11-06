package com.shivangee.jobportal.controller;

import com.shivangee.jobportal.dto.ApplicationResponse;
import com.shivangee.jobportal.dto.ApplyRequest;
import com.shivangee.jobportal.dto.PageResponse;
import com.shivangee.jobportal.dto.StatusUpdateRequest;
import com.shivangee.jobportal.service.ApplicationService;
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
@RequestMapping("/api")
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponse> apply(
            @PathVariable Long jobId,
            @Valid @RequestBody(required = false) ApplyRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(jobId, request, principal.getUsername()));
    }

    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public PageResponse<ApplicationResponse> myApplications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return applicationService.myApplications(principal.getUsername(), page, size);
    }

    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public PageResponse<ApplicationResponse> applicationsForJob(
            @PathVariable Long jobId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return applicationService.applicationsForJob(jobId, principal.getUsername(), page, size);
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
        return applicationService.updateStatus(id, request.status(), principal.getUsername());
    }
}
