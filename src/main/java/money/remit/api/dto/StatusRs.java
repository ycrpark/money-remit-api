package money.remit.api.dto;

import money.remit.api.common.StatusCode;
import lombok.Getter;

/**
 * 상태 값 있는 API RS 모델, API응답 및 통합 에러처리에 사용
 */
@Getter
public class StatusRs {
    private Status status;
    
    public StatusRs(StatusCode statusCode) {
        status = Status.builder().statusCode(statusCode).build();
    }
    
    public StatusRs(int code, String message) {
        status = new Status(code, message);
    }
}
