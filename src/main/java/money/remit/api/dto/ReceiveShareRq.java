package money.remit.api.dto;

import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * 받기 API RQ 모델
 */
@Getter
@Setter
public class ReceiveShareRq {
    // 뿌리기 고유 토큰
    @NotEmpty
    private String token;
}
