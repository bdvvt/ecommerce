package org.example.notificationservice.dto.wrapper;

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

    public static <T> ErrorResponse<T> of(Integer status, String error, T message, String path) {
        return ErrorResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
