package com.devcopilot.api.support;

import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.common.security.JwtPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class CurrentUserSupport {

    protected Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof JwtPrincipal principal) {
            return principal.userId();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
