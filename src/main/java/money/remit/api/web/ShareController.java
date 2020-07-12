package money.remit.api.web;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.remit.api.common.HeaderName;
import money.remit.api.common.StatusCode;
import money.remit.api.dto.CreateShareRq;
import money.remit.api.dto.CreateShareRs;
import money.remit.api.dto.ReceiveShareRq;
import money.remit.api.dto.ReceiveShareRs;
import money.remit.api.dto.SelectShareRs;
import money.remit.api.dto.ShareInfo;
import money.remit.api.exception.ShareException;
import money.remit.api.service.ShareService;

/**
 * 머니 뿌리기 관련
 */
@Slf4j
@RestController
@RequestMapping("/remit/share")
@AllArgsConstructor
public class ShareController {
    private ShareService shareService;
    
    /**
     * 뿌리기 생성 API
     * 
     * @param createShareRq
     * @param roomId 대화방ID
     * @param userId 사용자ID
     * @return 뿌리기 고유 토큰 발급
     */
    @PostMapping
    public CreateShareRs create(@RequestBody @Valid CreateShareRq createShareRq,
            @RequestHeader(value = HeaderName.ROOM_ID) String roomId,
            @RequestHeader(value = HeaderName.USER_ID) Long userId, Errors errors, HttpServletRequest request) {
        
        if (errors.hasErrors() || StringUtils.isEmpty(roomId) || userId == null) {
            return CreateShareRs.builder().statusCode(StatusCode.BAD_REQUEST).build();
        }
        
        try {
            String token = shareService.create(roomId, userId, createShareRq.getAmount(), createShareRq.getCount());
            log.info("createToken: {}", token);
            
            return CreateShareRs.builder().statusCode(StatusCode.OK).token(token).build();
        } catch (ShareException e) {
            log.warn("createShare: {}", e);
            return CreateShareRs.builder().statusCode(e.getStatusCode()).build();
        }
    }
    
    /**
     * 뿌리기 받기 API
     * 
     * @param receiveShareRq
     * @param roomId 대화방ID
     * @param userId 사용자ID
     * @return 받은 금액 반환
     */
    @PatchMapping
    public ReceiveShareRs receive(@RequestBody @Valid ReceiveShareRq receiveShareRq,
            @RequestHeader(value = HeaderName.ROOM_ID) String roomId,
            @RequestHeader(value = HeaderName.USER_ID) Long userId, Errors errors, HttpServletRequest request) {
        
        if (errors.hasErrors() || StringUtils.isEmpty(roomId) || userId == null) {
            return ReceiveShareRs.builder().statusCode(StatusCode.BAD_REQUEST).build();
        }
        
        try {
            Long amount = shareService.receive(roomId, userId, receiveShareRq.getToken());
            log.info("receiveAmount: {}", amount);
            
            return ReceiveShareRs.builder().statusCode(StatusCode.OK).amount(amount).build();
        } catch (ShareException e) {
            log.warn("receiveShare: {}", e);
            return ReceiveShareRs.builder().statusCode(e.getStatusCode()).build();
        }
    }
    
    /**
     * 뿌리기 상태 조회 API
     * 
     * @param token
     * @param roomId 대화방ID
     * @param userId 사용자ID
     * @param request
     * @return 시각, 금액, 받기 사용자 정보 등
     */
    @GetMapping
    public SelectShareRs select(String token, @RequestHeader(value = HeaderName.ROOM_ID) String roomId,
            @RequestHeader(value = HeaderName.USER_ID) Long userId, HttpServletRequest request) {
        
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(roomId) || userId == null) {
            return SelectShareRs.builder().statusCode(StatusCode.BAD_REQUEST).build();
        }
        
        try {
            ShareInfo shareInfo = shareService.select(roomId, userId, token);
            
            return SelectShareRs.builder().statusCode(StatusCode.OK).shareInfo(shareInfo).build();
        } catch (ShareException e) {
            log.warn("selectShare: {}", e);
            return SelectShareRs.builder().statusCode(e.getStatusCode()).build();
        }
    }
    
}
