package edu.duke.ece568.minUPS.service;

import edu.duke.ece568.minUPS.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final String upsID;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String upsID) {
        super(username, password, authorities);
        this.upsID = upsID;
    }

    public CustomUserDetails(Users user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getEmail(), user.getPassword(), authorities);
        this.upsID = user.getUpsID();
    }

    public String getUpsID() {
        return upsID;
    }
}