package com.nikitiuk.documentstoragewithframework.security;

import com.nikitiuk.documentstoragewithframework.entities.GroupBean;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserPrincipal implements Principal {

    private Long id;
    private String name;
    private Set<GroupBean> groups;

    public UserPrincipal(String name) {
        this.name = name;
    }

    public UserPrincipal() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<GroupBean> getGroups() {
        return groups;
    }

    public void setGroups(Set<GroupBean> groups) {
        this.groups = groups;
    }

    @Override
    public String getName() {
        return name;
    }
}