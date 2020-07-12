package money.remit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import money.remit.api.domain.ShareItem;

/**
 * 뿌리기 등록 정보 repo
 */
public interface ShareItemRepository extends JpaRepository<ShareItem, Long> {
    
}
