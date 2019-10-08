package com.nikitiuk.documentstoragewithframework.rest.services.helpers.dto;

import com.nikitiuk.documentstoragewithframework.entities.GroupBean;

import java.util.List;

public class UserDto {

    private String name;

    private String password;

    private List<GroupBean> groups;
}