package com.shivangee.jobportal.config;

import com.shivangee.jobportal.entity.Job;
import com.shivangee.jobportal.entity.Role;
import com.shivangee.jobportal.entity.User;
import com.shivangee.jobportal.repository.JobRepository;
import com.shivangee.jobportal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates demo users and jobs on startup so Swagger can be tried immediately. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, JobRepository jobRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createUser("Admin", "admin@jobportal.com", "Admin@123", Role.ADMIN);
        createUser("Demo Candidate", "candidate@jobportal.com", "Candidate@123", Role.CANDIDATE);
        User recruiter = createUser("Demo Recruiter", "recruiter@jobportal.com", "Recruiter@123", Role.RECRUITER);

        if (jobRepository.count() == 0) {
            createJob(recruiter, "Java Backend Developer", "Build REST APIs with Spring Boot and JPA.",
                    "TechNova Pvt Ltd", "Noida", 600000.0);
            createJob(recruiter, "Spring Boot Intern", "Work with senior developers on microservices.",
                    "CodeCraft", "Lucknow", 180000.0);
            createJob(recruiter, "Software Engineer (Java)", "Develop and test enterprise web applications.",
                    "InfoBridge", "Gurugram", 750000.0);
        }
    }

    private User createUser(String name, String email, String rawPassword, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setRole(role);
            return userRepository.save(u);
        });
    }

    private void createJob(User owner, String title, String description,
                           String company, String location, Double salary) {
        Job j = new Job();
        j.setTitle(title);
        j.setDescription(description);
        j.setCompany(company);
        j.setLocation(location);
        j.setSalary(salary);
        j.setPostedBy(owner);
        jobRepository.save(j);
    }
}
