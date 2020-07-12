package money.remit.api.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 등록된 뿌리기의 받아갈 각각의 정보
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareItem {
    
    @Id
    @GeneratedValue
    private Long id;
    
    // 소속된 뿌리기 토큰, 뿌리기 참조
    private String token;
    
    // 받았거나 받을 금액
    private Long amount;
    
    // 받아간 사용자 ID
    private Long userId;
    
    @Builder
    public ShareItem(String token, Long amount) {
        this.token = token;
        this.amount = amount;
    }
    
    // 받아가기 실행
    public void receive(Long userId) {
        this.userId = userId;
    }
    
}
