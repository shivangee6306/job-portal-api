package com.shivangee.jobportal.dto;

import com.shivangee.jobportal.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull ApplicationStatus status) {
}
