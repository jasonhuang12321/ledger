package com.learning;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public AppUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LedgerUserPrincipal principal) {
            return principal.getAppUser();
        }
        throw new BusinessException("未登录");
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(get().getRole());
    }
}
