package money.remit.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import money.remit.api.common.StatusCode;
import lombok.Builder;
import lombok.Getter;

/**
 * 뿌리기 상태조회 최종 RS 모델
 */
@Getter
public class SelectShareRs extends StatusRs {
    
    // 뿌린 시간
    private LocalDateTime shareDate;
    
    // 뿌린 금액
    private Long sharedAmount;
    
    // 받아간 금액
    private Long receivedAmount;
    
    // 받아간 사용자 정보
    private List<Receive> receives;
    
    @Builder
    public SelectShareRs(StatusCode statusCode, ShareInfo shareInfo) {
        super(statusCode);
        if(shareInfo != null) {
            shareDate = shareInfo.getShareDate();
            sharedAmount = shareInfo.getSharedAmount();
            receivedAmount = shareInfo.getReceivedAmount();
            receives = shareInfo.getReceives();
        }
    }
}
