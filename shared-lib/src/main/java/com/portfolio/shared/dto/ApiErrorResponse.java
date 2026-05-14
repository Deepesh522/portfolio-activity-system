package com.portfolio.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
                int status,
                String error,
                String message,
                String path,
                @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
                List<FieldError> fieldErrors) {
        @Builder
        public record FieldError(
                        String field,
                        String message,
                        Object rejectedValue) {
        }
}
