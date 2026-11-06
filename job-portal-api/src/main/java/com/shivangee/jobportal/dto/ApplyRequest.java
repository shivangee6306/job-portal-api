package com.shivangee.jobportal.dto;

import jakarta.validation.constraints.Size;

public record ApplyRequest(@Size(max = 2000) String coverLetter) {
}
