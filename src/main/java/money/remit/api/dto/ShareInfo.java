package money.remit.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 뿌리기 관련 정보
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareInfo {
    
    // 뿌린 시간
    private LocalDateTime shareDate;
    
    // 뿌린 금액
    private Long sharedAmount;
    
    // 받아간 금액
    private Long receivedAmount;
    
    // 받아간 사용자 정보
    private List<Receive> receives;
    
    @Builder
    public ShareInfo(LocalDateTime shareDate, Long sharedAmount, Long receivedAmount, List<Receive> receives) {
        this.shareDate = shareDate;
        this.sharedAmount = sharedAmount;
        this.receivedAmount = receivedAmount;
        this.receives = receives;
    }
}
