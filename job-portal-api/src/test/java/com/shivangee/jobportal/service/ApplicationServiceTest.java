package com.shivangee.jobportal.service;

import com.shivangee.jobportal.dto.ApplicationResponse;
import com.shivangee.jobportal.dto.ApplyRequest;
import com.shivangee.jobportal.entity.ApplicationStatus;
import com.shivangee.jobportal.entity.Job;
import com.shivangee.jobportal.entity.JobApplication;
import com.shivangee.jobportal.entity.Role;
import com.shivangee.jobportal.entity.User;
import com.shivangee.jobportal.exception.ConflictException;
import com.shivangee.jobportal.exception.ForbiddenException;
import com.shivangee.jobportal.repository.JobApplicationRepository;
import com.shivangee.jobportal.repository.JobRepository;
import com.shivangee.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User user(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setName("User " + id);
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    private Job job(Long id, User owner) {
        Job j = new Job();
        j.setId(id);
        j.setTitle("Java Dev");
        j.setDescription("desc");
        j.setCompany("Acme");
        j.setLocation("Noida");
        j.setPostedBy(owner);
        return j;
    }

    @Test
    void apply_firstTime_createsApplicationWithAppliedStatus() {
        User recruiter = user(1L, "r@x.com", Role.RECRUITER);
        User candidate = user(3L, "c@x.com", Role.CANDIDATE);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job(10L, recruiter)));
        when(userRepository.findByEmail("c@x.com")).thenReturn(Optional.of(candidate));
        when(applicationRepository.existsByJobIdAndCandidateId(10L, 3L)).thenReturn(false);
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> {
            JobApplication a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        ApplicationResponse res = applicationService.apply(10L, new ApplyRequest("Hire me"), "c@x.com");

        assertEquals(99L, res.id());
        assertEquals(ApplicationStatus.APPLIED, res.status());
        assertEquals("Hire me", res.coverLetter());
    }

    @Test
    void apply_twice_throwsConflict() {
        User recruiter = user(1L, "r@x.com", Role.RECRUITER);
        User candidate = user(3L, "c@x.com", Role.CANDIDATE);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job(10L, recruiter)));
        when(userRepository.findByEmail("c@x.com")).thenReturn(Optional.of(candidate));
        when(applicationRepository.existsByJobIdAndCandidateId(10L, 3L)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> applicationService.apply(10L, new ApplyRequest("again"), "c@x.com"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void updateStatus_byRecruiterWhoDoesNotOwnJob_isForbidden() {
        User owner = user(1L, "owner@x.com", Role.RECRUITER);
        User candidate = user(3L, "c@x.com", Role.CANDIDATE);
        JobApplication app = new JobApplication();
        app.setId(7L);
        app.setJob(job(10L, owner));
        app.setCandidate(candidate);
        when(applicationRepository.findById(7L)).thenReturn(Optional.of(app));

        assertThrows(ForbiddenException.class,
                () -> applicationService.updateStatus(7L, ApplicationStatus.SHORTLISTED, "other@x.com"));
        verify(applicationRepository, never()).save(any());
    }
}
