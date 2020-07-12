package money.remit.api.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 뿌리기 생성 API RQ 모델
 */
@Getter
@Setter
public class CreateShareRq {
    
    // 뿌릴 금액
    @NotNull
    @Min(1)
    private Long amount;
    
    // 뿌릴 인원 수
    @NotNull
    @Min(1)
    private Integer count;
}
