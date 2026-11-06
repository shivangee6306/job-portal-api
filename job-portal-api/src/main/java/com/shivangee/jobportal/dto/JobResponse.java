package com.shivangee.jobportal.dto;

import com.shivangee.jobportal.entity.Job;

import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        String title,
        String description,
        String company,
        String location,
        Double salary,
        boolean active,
        Long postedById,
        String postedByName,
        LocalDateTime createdAt) {

    public static JobResponse from(Job j) {
        return new JobResponse(
                j.getId(), j.getTitle(), j.getDescription(), j.getCompany(),
                j.getLocation(), j.getSalary(), j.isActive(),
                j.getPostedBy().getId(), j.getPostedBy().getName(), j.getCreatedAt());
    }
}
