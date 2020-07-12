package money.remit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import money.remit.api.domain.Share;

/**
 * 받아갔거나 받아갈 금액 및 정보 repo
 */
public interface ShareRepository extends JpaRepository<Share, Long> {
    
}
