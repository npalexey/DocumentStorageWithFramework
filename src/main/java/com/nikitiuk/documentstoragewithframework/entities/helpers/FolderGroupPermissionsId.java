package com.nikitiuk.documentstoragewithframework.entities.helpers;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FolderGroupPermissionsId implements Serializable {

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "folder_id")
    private Long folderId;

    public FolderGroupPermissionsId() {
    }

    public FolderGroupPermissionsId(
            Long groupId,
            Long folderId) {
        this.groupId = groupId;
        this.folderId = folderId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    @Override
    public String toString(){
        return "GroupId = " + groupId + " , FolderId = " + folderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FolderGroupPermissionsId that = (FolderGroupPermissionsId) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(folderId, that.folderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, folderId);
    }
}
