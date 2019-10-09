package com.nikitiuk.documentstoragewithframework.security;

import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.security.ContextDefiner;
import com.nikitiuk.javabeansinitializer.server.request.types.ISecurityContext;

@ContextDefiner
public class SecurityContext implements ISecurityContext {

    private UserPrincipal user;

    public SecurityContext(UserPrincipal user) {
        this.user = user;
    }

    @Override
    public UserPrincipal getUserPrincipal() {
        return this.user;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (user.getGroups() != null) {
            for (GroupBean groupBean : user.getGroups()) {
                if (groupBean.getName().equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }
}