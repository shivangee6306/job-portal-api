package com.shivangee.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank String title,
        @NotBlank @Size(max = 4000) String description,
        @NotBlank String company,
        @NotBlank String location,
        @PositiveOrZero Double salary) {
}
