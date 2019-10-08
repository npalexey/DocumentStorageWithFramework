package com.nikitiuk.documentstoragewithframework.entities;

import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Folders")
@NaturalIdCache
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
public class FolderBean {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @Column(name = "folder_path", unique = true, nullable = false)
    private String path;

    @OneToMany(
            mappedBy = "folder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<FolderGroupPermissions> foldersPermissions = new HashSet<>();

    public FolderBean() {
    }

    public FolderBean(String path) {
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<FolderGroupPermissions> getFoldersPermissions() {
        return foldersPermissions;
    }

    public void setFoldersPermissions(Set<FolderGroupPermissions> foldersPermissions) {
        this.foldersPermissions = foldersPermissions;
    }

    public void addGroup(GroupBean group, Permissions permissions) {
        if (checkIfFolderHasGroup(group)) {
            return;
        }
        FolderGroupPermissions folderGroupPermissions = new FolderGroupPermissions(group, this);
        folderGroupPermissions.setPermissions(permissions);
        foldersPermissions.add(folderGroupPermissions);
    }

    public boolean checkIfFolderHasGroup(GroupBean group) {
        for (FolderGroupPermissions folderGroupPermissions : foldersPermissions) {
            if (folderGroupPermissions.getFolder().equals(this) &&
                    folderGroupPermissions.getGroup().equals(group)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Folder [folder_id=" + id + ", folder_path=" + path + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FolderBean folderBean = (FolderBean) o;
        return Objects.equals(path, folderBean.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
