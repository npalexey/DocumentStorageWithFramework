package com.nikitiuk.documentstoragewithframework.security;

import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.security.ContextDefiner;

@ContextDefiner
public class SecurityContext {

    private UserPrincipal user;

    public SecurityContext(UserPrincipal user) {
        this.user = user;
    }

    public UserPrincipal getUserPrincipal() {
        return this.user;
    }

    public boolean isUserInRole(String s) {
        if (user.getGroups() != null) {
            for (GroupBean groupBean : user.getGroups()) {
                if (groupBean.getName().equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }
}