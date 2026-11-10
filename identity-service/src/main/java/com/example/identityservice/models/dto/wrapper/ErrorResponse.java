package com.example.identityservice.models.dto.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ErrorResponse<T> {
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private Integer status;
    private String error;
    private T message;
    private String path;
}
