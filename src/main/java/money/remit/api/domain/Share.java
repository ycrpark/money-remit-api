package money.remit.api.domain;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 뿌리기 모델
 */
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Share {
    
    // 뿌리기 고유 번호
    @Id
    private Long id;
    
    // id를 기준으로 생성한 부리기 고유 토큰
    private String token;
    
    // 방ID
    private String roomId;
    
    // 사용자ID
    private Long userId;
    
    // 생성일시
    @CreatedDate
    private LocalDateTime createDate;
    
    @Builder
    public Share(Long id, String token, String roomId, Long userId) {
        this.id = id;
        this.token = token;
        this.roomId = roomId;
        this.userId = userId;
    }
    
}
