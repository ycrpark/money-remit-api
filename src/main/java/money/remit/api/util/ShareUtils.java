package money.remit.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 뿌리기 관련 유틸
 */
public class ShareUtils {
    
    // 토큰 문자열 진법 수, 알파뱃 대소문자(52) + 숫자(10)
    private static final int NOTATION = 62;
    
    /**
     * 금액을 랜덤하게 분할하여 반환
     * 
     * <pre>
     * devideAmount(10000, 5) = [1000, 2000, 3000, 1500, 2500]
     * </pre>
     * 
     * @param amount 분할할 금액
     * @param count 나눌 개수
     * @return
     */
    public static List<Long> devideAmount(long amount, int count) {
        // 분할 기준 랜덤 생성
        List<Long> pivots = Stream.generate(() -> ThreadLocalRandom.current().nextLong(amount + 1))
                .limit(count - 1)
                .sorted()
                .collect(Collectors.toList());
        
        List<Long> devidedAmounts = new ArrayList<>();
        
        // 기준 사이 간격으로 분할
        long beforePivit = 0;
        for (long pivot : pivots) {
            devidedAmounts.add(pivot - beforePivit);
            beforePivit = pivot;
        }
        
        devidedAmounts.add(amount - beforePivit);
        return devidedAmounts;
    }
    
    /**
     * 뿌리기 토큰 생성에 사용되는 ID 생성
     * 
     * @param ids 기존 생성된 ID들
     * @param tokenLength 생성할 토큰 길이
     * @return
     */
    public static long generateId(List<Long> ids, int tokenLength) {
        // 토큰 길이에 따른 생성가능 잔여 수
        long range = (long) Math.pow(NOTATION, tokenLength);
        long ableCount = range - ids.size();
        
        // 기존 ID와 중복되지 않도록
        long generatedId = ThreadLocalRandom.current().nextLong(ableCount) + 1;
        Collections.sort(ids);
        for (Long id : ids) {
            if (id > generatedId) {
                break;
            }
            
            generatedId++;
        }
        return generatedId;
    }
    
    /**
     * 뿌리기 토큰 생성
     * 
     * <pre>
     * 0 < n 숫자를 52진법 문자열(알파뱃 대소문자 + 숫자)로 변경
     * generateToken(0, 3)  = null
     * generateToken(1, 3)  = "AAA"
     * generateToken(2, 3)  = "AAB"
     * generateToken(3, 3)  = "AAC"
     * generateToken(238328, 3)  = "zzz"
     * </pre>
     * 
     * @param id 숫자ID
     * @param tokenLength 생성할 토큰 길이
     * @return
     */
    public static String generateToken(long id, int tokenLength) {
        // 토큰 길이로 생성가능한 숫자 범위
        long range = (long) Math.pow(NOTATION, tokenLength);
        if (tokenLength > 10 || id < 1 || id > range) {
            return null;
        }
        
        // 자리수 마추기 위한 기본값
        long baseNumber = -1;
        for (int i = 1; i <= tokenLength; i++) {
            baseNumber += (long) Math.pow(NOTATION, i);
        }
        
        long caculNum = id + baseNumber;
        char[] buf = new char[tokenLength];
        
        for (int i = tokenLength - 1; i >= 0; i--) {
            long curr = caculNum % NOTATION + 48;
            // 숫자 영역 초과시 알파뱃영역으로
            if (curr > 57) {
                curr += 7;
            }
            
            // 대문자영역 초과시 소문자 영역으로
            if (curr > 90) {
                curr += 6;
            }
            
            buf[i] = (char) curr;
            caculNum = caculNum / NOTATION - 1;
        }
        
        return String.valueOf(buf);
    }
    
}
