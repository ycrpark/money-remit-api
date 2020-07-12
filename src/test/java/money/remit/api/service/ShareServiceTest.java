package money.remit.api.service;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import lombok.extern.slf4j.Slf4j;
import money.remit.api.common.StatusCode;
import money.remit.api.domain.Share;
import money.remit.api.domain.ShareItem;
import money.remit.api.dto.Receive;
import money.remit.api.dto.ShareInfo;
import money.remit.api.exception.ShareException;
import money.remit.api.repository.ShareItemRepository;
import money.remit.api.repository.ShareRepository;
import money.remit.api.util.ShareUtils;

@Slf4j
@SpringBootTest
public class ShareServiceTest {
    private static final int NOTATION = 62;
    private static final int TOKEN_LENGTH = 3;
    
    private static final String ROOM_ID_1 = "RID1";
    private static final String ROOM_ID_2 = "RID2";
    private static final Long USER_ID_1 = 1000L;
    private static final Long USER_ID_2 = 2000L;
    private static final Long AMOUNT_1 = 100000L;
    private static final Long AMOUNT_2 = 10000000L;
    private static final Integer COUNT_1 = 10;
    private static final Integer COUNT_2 = 50;
    
    @Autowired
    private ShareService shareService;
    
    @Autowired
    private ShareRepository shareRepository;
    
    @Autowired
    private ShareItemRepository shareItemRepository;
    
    /**
     * 토큰 복원
     */
    private static Long restoreToken(String token) {
        if (token == null) {
            return null;
        }
        
        char[] buf = token.toCharArray();
        
        long digit = 1;
        long result = 0;
        for (int i = token.length() - 1; i >= 0; i--) {
            long curr = buf[i];
            if (curr > 96) {
                curr -= 6;
            }
            
            if (curr > 64) {
                curr -= 7;
            }
            
            curr = (curr - 48) * digit;
            
            result += curr;
            digit *= NOTATION;
        }
        
        return result + 1;
    }
    
    /**
     * 토큰복원 로직 테스트
     */
    @Test
    public void generateAndRestoreToken() {
        for(int i = 1; i < 140609; i++) {
            String alp = ShareUtils.generateToken(i, 3);
            long no = restoreToken(alp);
            assertTrue(no == i);
        }
    }
    
    /**
     * 뿌리기
     */
    @Test
    public void createTest() {
        
        // 제약조건 오류 처리 체크
        Exception e = assertThrows(ShareException.class, () -> shareService.create(null, USER_ID_1, AMOUNT_1, COUNT_1));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        e = assertThrows(ShareException.class, () -> shareService.create(ROOM_ID_1, null, AMOUNT_1, COUNT_1));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        e = assertThrows(ShareException.class, () -> shareService.create(ROOM_ID_1, USER_ID_1, null, COUNT_1));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        e = assertThrows(ShareException.class, () -> shareService.create(ROOM_ID_1, USER_ID_1, AMOUNT_1, null));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        // 토큰 정상 발급, 토큰 길이, 뿌리기 저장, 토큰복원 체크
        String token = shareService.create(ROOM_ID_1, USER_ID_1, AMOUNT_1, COUNT_1);
        assertTrue(token != null && token.length() == TOKEN_LENGTH && restoreToken(token) != null);
        Share share = shareRepository.findOne(Example.of(Share.builder().roomId(ROOM_ID_1).token(token).build())).orElse(null);
        assertTrue(share != null && share.getToken() != null && share.getToken().equals(token));
        
        token = shareService.create(ROOM_ID_2, USER_ID_2, AMOUNT_2, COUNT_2);
        assertTrue(token != null && token.length() == TOKEN_LENGTH && restoreToken(token) != null);
        share = shareRepository.findOne(Example.of(Share.builder().roomId(ROOM_ID_2).token(token).build())).orElse(null);
        assertTrue(share != null && share.getToken() != null && share.getToken().equals(token));
        
        // 분배 항목 체크
        List<ShareItem> items = shareItemRepository.findAll(Example.of(ShareItem.builder().token(token).build()));
        assertTrue(!items.isEmpty());
        for(ShareItem item : items) {
            assertTrue(item.getAmount() != null && item.getId() != null && item.getToken() != null && item.getUserId() == null);
        }
        
        for(int i = 0; i < 100; i++) {
            long amount = ThreadLocalRandom.current().nextLong(1000000000L);
            int count = (int) ThreadLocalRandom.current().nextLong(10000L);
            
            token = shareService.create(ROOM_ID_1, USER_ID_1, amount, count);
            // 토큰 정상 발급, 복원 체크
            assertTrue(token != null && token.length() == TOKEN_LENGTH && restoreToken(token) != null);
            
            // 저장 체크
            share = shareRepository.findOne(Example.of(Share.builder().roomId(ROOM_ID_1).token(token).build())).orElse(null);
            assertTrue(share != null && share.getToken() != null && share.getToken().equals(token));
            
            // 분배 항목 체크
            items = shareItemRepository.findAll(Example.of(ShareItem.builder().token(token).build()));
            assertTrue(!items.isEmpty());
            for(ShareItem item : items) {
                assertTrue(item.getAmount() != null && item.getId() != null && item.getToken() != null && item.getUserId() == null);
            }
        }
        
    }
    
    /**
     * 받기
     */
    @Test
    public void receiveTest() {
        String token2 = shareService.create(ROOM_ID_1, USER_ID_1, AMOUNT_1, COUNT_1);
        
        // 에러 처리 체크
        Exception e = assertThrows(ShareException.class, () -> shareService.receive(null, USER_ID_2, token2));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        e = assertThrows(ShareException.class, () -> shareService.receive(ROOM_ID_1, null, token2));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        e = assertThrows(ShareException.class, () -> shareService.receive(ROOM_ID_1, USER_ID_2, null));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        // 동작
        Long amount = shareService.receive(ROOM_ID_1, USER_ID_2, token2);
        assertTrue(amount != null && amount > 0);
        
        // 제약조건 오류 처리 체크
        String token = shareService.create(ROOM_ID_1, USER_ID_1, AMOUNT_1, COUNT_1);
        
        // 본인 여부
        e = assertThrows(ShareException.class, () -> shareService.receive(ROOM_ID_1, USER_ID_1, token));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.FORBIDDEN);
        
        // 동일인 2회 시도
        amount = shareService.receive(ROOM_ID_1, USER_ID_2, token);
        assertTrue(amount != null && amount > 0);
        e = assertThrows(ShareException.class, () -> shareService.receive(ROOM_ID_1, USER_ID_2, token));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.FORBIDDEN);
        
        // 다른 방에서 시도
        e = assertThrows(ShareException.class, () -> shareService.receive(ROOM_ID_2, USER_ID_2, token));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.UNAUTHORIZED);
        
        // 유효시간 체크
        Share share = shareRepository.findOne(Example.of(Share.builder().roomId(ROOM_ID_1).token(token).build())).orElse(null);
        assertTrue(share != null && share.getToken() != null && share.getToken().equals(token));
        log.info("time: {}", share.getCreateDate());
        assertTrue(share.getCreateDate().isBefore(LocalDateTime.now()));
        assertTrue(share.getCreateDate().isAfter(LocalDateTime.now().minusMinutes(10)));
    }
    
    /**
     * 조회
     */
    @Test
    public void selectTest() {
        // 생성, 받기
        String token2 = shareService.create(ROOM_ID_1, USER_ID_1, AMOUNT_1, COUNT_1);
        Long amount = shareService.receive(ROOM_ID_1, USER_ID_2, token2);
        assertTrue(amount != null && amount > 0);
        
        // 에러 처리 체크
        Exception e = assertThrows(ShareException.class, () -> shareService.select(null, USER_ID_1, token2));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        e = assertThrows(ShareException.class, () -> shareService.select(ROOM_ID_1, null, token2));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        e = assertThrows(ShareException.class, () -> shareService.select(ROOM_ID_1, USER_ID_1, null));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.BAD_REQUEST);
        
        
        // 데이터 확인
        ShareInfo shareInfo = shareService.select(ROOM_ID_1, USER_ID_1, token2);
        assertTrue(shareInfo != null && shareInfo.getReceivedAmount() != null && shareInfo.getReceivedAmount() >= 0);
        assertTrue(shareInfo.getSharedAmount() != null && shareInfo.getSharedAmount() == AMOUNT_1);
        assertTrue(shareInfo.getShareDate() != null);
        assertTrue(shareInfo.getReceives() != null);
        for(Receive receive : shareInfo.getReceives()) {
            assertTrue(receive.getAmount() != null && receive.getAmount() == amount.longValue());
            assertTrue(receive.getUserId() != null);
        }
        
        // 본인 여부
        e = assertThrows(ShareException.class, () -> shareService.select(ROOM_ID_1, USER_ID_2, token2));
        assertTrue(e instanceof ShareException && ((ShareException) e).getStatusCode() == StatusCode.UNAUTHORIZED);
        
        // 유효시간 체크
        assertTrue(shareInfo.getShareDate().isBefore(LocalDateTime.now()));
        assertTrue(shareInfo.getShareDate().isAfter(LocalDateTime.now().minusDays(7)));
    }
}
