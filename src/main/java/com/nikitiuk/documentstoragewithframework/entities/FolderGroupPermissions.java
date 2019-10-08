package com.nikitiuk.documentstoragewithframework.entities;

import com.nikitiuk.documentstoragewithframework.entities.helpers.FolderGroupPermissionsId;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Folder_group_permissions")
public class FolderGroupPermissions {

    @EmbeddedId
    private FolderGroupPermissionsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    private GroupBean group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("folderId")
    private FolderBean folder;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_permissions_for_folder", nullable = false)
    private Permissions permissions;

    public FolderGroupPermissions() {

    }

    public FolderGroupPermissions(GroupBean group, FolderBean folder) {
        this.group = group;
        this.folder = folder;
        this.id = new FolderGroupPermissionsId(group.getId(), folder.getId());
    }

    public FolderGroupPermissions(GroupBean group, FolderBean folder, Permissions permissions) {
        this.group = group;
        this.folder = folder;
        this.id = new FolderGroupPermissionsId(group.getId(), folder.getId());
        this.permissions = permissions;
    }

    public FolderGroupPermissionsId getId() {
        return id;
    }

    public void setId(FolderGroupPermissionsId id) {
        this.id = id;
    }

    public GroupBean getGroup() {
        return group;
    }

    public void setGroup(GroupBean group) {
        this.group = group;
    }

    public FolderBean getFolder() {
        return folder;
    }

    public void setFolder(FolderBean folder) {
        this.folder = folder;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString(){
        return /*"GroupId = " + group.getId() + ", DocId = " + document.getId()*/id + " , " + permissions;
        //return "Document Permission: [gourp_id=" + group + ", document_id=" + document + ", group_permissions=" + permissions.toString() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FolderGroupPermissions that = (FolderGroupPermissions) o;
        return Objects.equals(group, that.group) && Objects.equals(folder, that.folder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, folder);
    }
}
