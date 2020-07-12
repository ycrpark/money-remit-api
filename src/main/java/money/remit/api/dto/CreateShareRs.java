package money.remit.api.dto;

import money.remit.api.common.StatusCode;
import lombok.Builder;
import lombok.Getter;

/**
 * 뿌리기 생성 API RS 모델
 */
@Getter
public class CreateShareRs extends StatusRs {
    
    // 발급된 뿌리기 고유 토큰
    private String token;
    
    @Builder
    public CreateShareRs(StatusCode statusCode, String token) {
        super(statusCode);
        this.token = token;
    }
}
