package com.shivangee.jobportal.service;

import com.shivangee.jobportal.dto.JobRequest;
import com.shivangee.jobportal.dto.JobResponse;
import com.shivangee.jobportal.entity.Job;
import com.shivangee.jobportal.entity.Role;
import com.shivangee.jobportal.entity.User;
import com.shivangee.jobportal.exception.ForbiddenException;
import com.shivangee.jobportal.exception.ResourceNotFoundException;
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
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobService jobService;

    private User user(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setName("Test " + id);
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
    void create_savesJobForRecruiter() {
        User recruiter = user(1L, "r@x.com", Role.RECRUITER);
        when(userRepository.findByEmail("r@x.com")).thenReturn(Optional.of(recruiter));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(5L);
            return j;
        });

        JobResponse res = jobService.create(
                new JobRequest("Java Dev", "desc", "Acme", "Noida", 600000.0), "r@x.com");

        assertEquals(5L, res.id());
        assertEquals("Java Dev", res.title());
        assertEquals(1L, res.postedById());
    }

    @Test
    void update_byAnotherRecruiter_isForbidden() {
        User owner = user(1L, "owner@x.com", Role.RECRUITER);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job(10L, owner)));

        assertThrows(ForbiddenException.class, () -> jobService.update(
                10L, new JobRequest("New", "desc", "Acme", "Noida", 1.0), "other@x.com"));
        verify(jobRepository, never()).save(any());
    }

    @Test
    void close_byOwner_setsJobInactive() {
        User owner = user(1L, "owner@x.com", Role.RECRUITER);
        Job j = job(10L, owner);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(j));
        when(userRepository.findByEmail("owner@x.com")).thenReturn(Optional.of(owner));

        jobService.close(10L, "owner@x.com");

        assertEquals(false, j.isActive());
        verify(jobRepository).save(j);
    }

    @Test
    void getById_closedJob_throwsNotFound() {
        Job j = job(10L, user(1L, "owner@x.com", Role.RECRUITER));
        j.setActive(false);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(j));

        assertThrows(ResourceNotFoundException.class, () -> jobService.getById(10L));
    }
}
