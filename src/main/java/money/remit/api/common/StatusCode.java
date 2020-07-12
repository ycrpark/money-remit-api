package money.remit.api.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 응답 상태 정보
 */
@Getter
@AllArgsConstructor
public enum StatusCode {
    // --- 2xx Success ---
    OK(200, "OK"),
    NO_CONTENT(204, "NO_CONTENT"),
    
    // --- 4xx Client Error ---
    BAD_REQUEST(400, "BAD_REQUEST"),
    UNAUTHORIZED(401, "UNAUTHORIZED"),
    FORBIDDEN(403, "FORBIDDEN"),
    GONE(410, "GONE"),
    
    // --- 5xx Server Error ---
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR"),
    UNKNOWN_ERROR(520, "UNKNOWN_ERROR");
    
    private Integer code;
    private String message;
}
