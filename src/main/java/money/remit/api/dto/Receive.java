package money.remit.api.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 뿌리기 받아간 사용자 정보
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receive {
    
    // 받아간 금액
    private Long amount;
    
    // 받아간 사용자 ID
    private Long userId;
    
    @Builder
    public Receive(Long amount, Long userId) {
        super();
        this.amount = amount;
        this.userId = userId;
    }
}
