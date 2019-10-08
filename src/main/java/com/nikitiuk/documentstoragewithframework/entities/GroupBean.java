package com.nikitiuk.documentstoragewithframework.entities;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Permission_groups")
@NaturalIdCache
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
public class GroupBean implements Serializable {

    @ManyToMany(mappedBy = "groups", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @OrderBy //same as @OrderBy("id ASC")
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<UserBean> users = new HashSet<>();

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @Column(name = "group_name", unique = true, nullable = false)
    private String name;

    public GroupBean(String name) {
        this.name = name;
    }

    public GroupBean() {

    }

    public Set<UserBean> getUsers() {
        return users;
    }

    public void setUsers(Set<UserBean> users) {
        this.users = users;
        for (UserBean user : users) {
            user.getGroups().add(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*public void addFolder(FolderBean folder, Permissions permissions) {
        if (checkIfGroupHasFolder(folder)) {
            return;
        }
        FolderGroupPermissions folderGroupPermissions = new FolderGroupPermissions(this, folder);
        folderGroupPermissions.setPermissions(permissions);
        foldersPermissions.add(folderGroupPermissions);
    }

    public boolean checkIfGroupHasFolder(FolderBean folder) {
        for (FolderGroupPermissions folderGroupPermissions : foldersPermissions) {
            if (folderGroupPermissions.getGroup().equals(this) &&
                    folderGroupPermissions.getFolder().equals(folder)) {
                return true;
            }
        }
        return false;
    }*/

    /*public void addDocument(DocBean document, Permissions permissions) {
        if (checkIfGroupHasDocument(document)) {
            return;
        }
        DocGroupPermissions docGroupPermissions = new DocGroupPermissions(this, document);
        docGroupPermissions.setPermissions(permissions);
        documentsPermissions.add(docGroupPermissions);
    }
    public boolean checkIfGroupHasDocument(DocBean document) {
        for (DocGroupPermissions docGroupPermissions : documentsPermissions) {
            if (docGroupPermissions.getGroup().equals(this) &&
                    docGroupPermissions.getDocument().equals(document)) {
                return true;
            }
        }
        return false;
    }*/

    public Set<String> getUserNamesSet() {
        Set<String> userNames = new HashSet<>();
        if (CollectionUtils.isNotEmpty(users)) {
            for (UserBean user : users) {
                userNames.add(user.getName());
            }
        }
        return userNames;
    }

    @Override
    public String toString() {
        return "Group [gourp_id=" + id + ", group_name=" + name + /*", document_permissions=" + documentsPermissions.toString() + *//*", folder_permissions=" + foldersPermissions.toString() + */"]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupBean groupBean = (GroupBean) o;
        return Objects.equals(name, groupBean.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}