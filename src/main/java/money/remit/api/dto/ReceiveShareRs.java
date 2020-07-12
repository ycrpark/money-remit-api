package money.remit.api.dto;

import money.remit.api.common.StatusCode;
import lombok.Builder;
import lombok.Getter;

/**
 * 받기 API RS 모델
 */
@Getter
public class ReceiveShareRs extends StatusRs {
    
    // 받은 금액
    private Long amount;
    
    @Builder
    public ReceiveShareRs(StatusCode statusCode, Long amount) {
        super(statusCode);
        this.amount = amount;
    }
}
