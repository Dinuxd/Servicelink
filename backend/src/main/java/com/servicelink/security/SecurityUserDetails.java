package com.servicelink.security;

import com.servicelink.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityUserDetails implements UserDetails {
    private final User user;
    private final List<GrantedAuthority> authorities;

    public SecurityUserDetails(User user) {
        this.user = user;
        this.authorities = user.getRoleNames() == null ? List.of() :
            user.getRoleNames().stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }
    public String getUsernameField() { return user.getUsername(); }
    public List<String> getRoleNames() { return user.getRoleNames(); }
    public User getUser() { return user; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security uses this for authentication principal
        // We'll allow login by username or email; fall back to email when username is missing
        return user.getUsername() != null ? user.getUsername() : user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return user.isActive(); }
}
