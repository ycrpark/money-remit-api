package money.remit.api.exception;

import money.remit.api.common.StatusCode;
import lombok.Getter;

/**
 * 뿌리기 관련 Exception
 */
@Getter
public class ShareException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private StatusCode statusCode;
    
    public ShareException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }
}
