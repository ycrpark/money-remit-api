package money.remit.api.web;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import money.remit.api.common.StatusCode;
import money.remit.api.dto.StatusRs;

/**
 * error 발생 시 응답 처리
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrorController extends AbstractErrorController {
    private final ErrorProperties errorProperties;
    
    public ErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties,
            List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
        
        Assert.notNull(serverProperties.getError(), "ErrorProperties must not be null");
        this.errorProperties = serverProperties.getError();
    }
    
    @Override
    public String getErrorPath() {
        return this.errorProperties.getPath();
    }
    
    @RequestMapping
    public ResponseEntity<StatusRs> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        // 500번대 숨김
        if (status.is5xxServerError()) {
            return ResponseEntity.ok(new StatusRs(StatusCode.UNKNOWN_ERROR));
        }
        
        return new ResponseEntity<>(new StatusRs(status.value(), status.getReasonPhrase()), status);
    }
}
