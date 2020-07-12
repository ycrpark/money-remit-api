package money.remit.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import money.remit.api.common.StatusCode;
import money.remit.api.domain.Share;
import money.remit.api.domain.ShareItem;
import money.remit.api.dto.Receive;
import money.remit.api.dto.ShareInfo;
import money.remit.api.exception.ShareException;
import money.remit.api.repository.ShareItemRepository;
import money.remit.api.repository.ShareRepository;
import money.remit.api.util.ShareUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 머니 뿌리기 관련 서비스
 */
@Slf4j
@Service
@AllArgsConstructor
public class ShareService {
    private static final int TOKEN_LENGTH = 3;
    
    private ShareRepository shareRepository;
    private ShareItemRepository shareItemRepository;
    
    /**
     * 뿌리기 생성
     * 
     * @param roomId 방ID
     * @param userId 뿌리기 생성자 ID
     * @param amount 뿌릴 금액
     * @param count 뿌릴 인원 수
     * @return 고유 토큰
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String create(String roomId, Long userId, Long amount, Integer count) {
        if (roomId == null || userId == null || amount == null || amount <= 0 || count == null || count < 1) {
            throw new ShareException(StatusCode.BAD_REQUEST);
        }
        
        // 기존 발급된 ID들
        List<Long> ids = shareRepository.findAll().stream()
                .map(Share::getId)
                .collect(Collectors.toList());
        log.debug("exists id count: {}", ids.size());
        
        // id, token 생성
        long id = ShareUtils.generateId(ids, TOKEN_LENGTH);
        String token = ShareUtils.generateToken(id, TOKEN_LENGTH);
        log.debug("token: {}", token);
        
        // 뿌리기 생성
        Share share = Share.builder()
                .id(id)
                .token(token)
                .roomId(roomId)
                .userId(userId)
                .build();
        shareRepository.save(share);
        
        // 분배할 랜덤 금액
        List<Long> devideAmounts = ShareUtils.devideAmount(amount, count);
        List<ShareItem> shareItems = devideAmounts.stream()
                .map(shareItem -> ShareItem.builder().token(token).amount(shareItem).build())
                .collect(Collectors.toList());
        
        // 뿌릴 금액 인원수만큼 저장
        shareItemRepository.saveAll(shareItems);
        return token;
    }
    
    /**
     * 뿌린 금액 받기
     * 
     * @param roomId 방ID
     * @param userId 뿌리기 생성자 ID
     * @param token 뿌리기 고유 토큰
     * @return 받은 금액
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long receive(String roomId, Long userId, String token) {
        if (roomId == null || userId == null || token == null) {
            throw new ShareException(StatusCode.BAD_REQUEST);
        }
        
        // 생성된 뿌리기 조회
        Share share = shareRepository.findOne(Example.of(Share.builder().roomId(roomId).token(token).build()))
                .orElseThrow(() -> new ShareException(StatusCode.UNAUTHORIZED));
        
        // 본인 제외
        if (userId.intValue() == share.getUserId()) {
            log.info("try receive own: {}", userId);
            throw new ShareException(StatusCode.FORBIDDEN);
        }
        
        // 유효 시간 체크
        if (share.getCreateDate().isBefore(LocalDateTime.now().minusMinutes(10))) {
            log.info("expired: {}, {}", userId, share.getCreateDate());
            throw new ShareException(StatusCode.GONE);
        }
        
        // 분배된 금액 정보 조회
        List<ShareItem> shareItems =
                shareItemRepository.findAll(Example.of(ShareItem.builder().token(token).build()), Sort.by("id"));
        if (shareItems.isEmpty()) {
            log.error("shareItems is empty: {}, {}, {}", roomId, userId, token);
            throw new ShareException(StatusCode.INTERNAL_SERVER_ERROR);
        }
        
        // 받은 적 있는지 체크
        if (shareItems.stream().anyMatch(
                shareItem -> shareItem.getUserId() != null && userId.intValue() == shareItem.getUserId().intValue())) {
            log.info("already received: {}", userId);
            throw new ShareException(StatusCode.FORBIDDEN);
        }
        
        // 안받아간 뿌리기 선택, 저장
        ShareItem shareItem = shareItems.stream()
                .filter(item -> item.getUserId() == null)
                .findFirst()
                .orElseThrow(() -> new ShareException(StatusCode.GONE));
        
        // 받음 저장
        shareItem.receive(userId);
        shareItemRepository.save(shareItem);
        
        // 받은 금액 반환
        return shareItem.getAmount();
    }
    
    /**
     * 뿌리기 진행 상태 조회
     * 
     * @param roomId 방ID
     * @param userId 뿌리기 생성자 ID
     * @param token 뿌리기 고유 토큰
     * @return
     */
    public ShareInfo select(String roomId, Long userId, String token) {
        if (roomId == null || userId == null || token == null) {
            throw new ShareException(StatusCode.BAD_REQUEST);
        }
        
        // 생성된 뿌리기 조회
        Share share = shareRepository.findOne(Example.of(Share.builder().roomId(roomId).userId(userId).token(token).build()))
                .orElseThrow(() -> new ShareException(StatusCode.UNAUTHORIZED));
        
        // 조회 가능 날짜 체크
        if (share.getCreateDate().isBefore(LocalDateTime.now().minusDays(7))) {
            log.info("expired: {}, {}", userId, share.getCreateDate());
            throw new ShareException(StatusCode.GONE);
        }
        
        // 분배된 금액 정보 조회
        List<ShareItem> shareItems =
                shareItemRepository.findAll(Example.of(ShareItem.builder().token(token).build()), Sort.by("id"));
        if (shareItems.isEmpty()) {
            log.error("shareItems is empty: {}, {}, {}", roomId, userId, token);
            throw new ShareException(StatusCode.INTERNAL_SERVER_ERROR);
        }
        
        // 뿌리기 총 금액, 받아간 금액
        long sharedAmount = shareItems.stream().mapToLong(ShareItem::getAmount).sum();
        long receivedAmount = shareItems.stream()
                .filter(item -> item.getUserId() != null)
                .mapToLong(ShareItem::getAmount)
                .sum();
        log.debug("sharedAmount: {}", sharedAmount);
        log.debug("receivedAmount: {}", receivedAmount);
        
        // 받아간 사람들 정보
        List<Receive> receives = shareItems.stream()
                .filter(item -> item.getUserId() != null)
                .map(item -> Receive.builder()
                        .amount(item.getAmount())
                        .userId(item.getUserId())
                        .build())
                .collect(Collectors.toList());
        log.debug("receives size: {}", receives.size());
        
        // 뿌리기 정보
        ShareInfo shareInfo = ShareInfo.builder()
                .shareDate(share.getCreateDate())
                .sharedAmount(sharedAmount)
                .receivedAmount(receivedAmount)
                .receives(receives)
                .build();
        
        return shareInfo;
    }
}
