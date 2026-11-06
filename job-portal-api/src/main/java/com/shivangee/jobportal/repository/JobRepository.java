package com.shivangee.jobportal.repository;

import com.shivangee.jobportal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

    // Empty keyword/location ("") matches everything.
    @Query("SELECT j FROM Job j WHERE j.active = true "
            + "AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "     OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Job> search(@Param("keyword") String keyword,
                     @Param("location") String location,
                     Pageable pageable);

    Page<Job> findByPostedById(Long userId, Pageable pageable);
}
