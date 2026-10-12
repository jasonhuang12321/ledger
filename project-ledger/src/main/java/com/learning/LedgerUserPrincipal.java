package com.learning;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class LedgerUserPrincipal implements UserDetails {

    private final AppUser user;

    public LedgerUserPrincipal(AppUser user) {
        this.user = user;
    }

    public AppUser getAppUser() {
        return user;
    }

    @Override
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole() == null ? "USER" : user.getRole();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
