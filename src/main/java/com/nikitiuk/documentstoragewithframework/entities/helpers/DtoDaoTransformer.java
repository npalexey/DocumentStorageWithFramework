package com.nikitiuk.documentstoragewithframework.entities.helpers;

import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.dto.FolderDto;
import com.nikitiuk.documentstoragewithframework.security.UserPrincipal;

public class DtoDaoTransformer {

    public UserBean userPrincipalToUserBean(UserPrincipal userPrincipal) {
        UserBean userBean = new UserBean();
        userBean.setId(userPrincipal.getId());
        userBean.setName(userPrincipal.getName());
        userBean.setGroups(userPrincipal.getGroups());
        return userBean;
    }

    public FolderBean folderDtoToFolderBean(FolderDto folderDto) {
        FolderBean folderBean = new FolderBean();
        folderBean.setId(folderDto.getId());
        folderBean.setPath(folderDto.getPath());
        return folderBean;
    }
}
