package com.nikitiuk.documentstoragewithframework.entities;

import com.nikitiuk.documentstoragewithframework.entities.helpers.DocGroupPermissionsId;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Document_group_permissions")
public class DocGroupPermissions {

    @EmbeddedId
    private DocGroupPermissionsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    private GroupBean group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("documentId")
    private DocBean document;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_permissions_for_doc", nullable = false)
    private Permissions permissions;

    public DocGroupPermissions() {

    }

    public DocGroupPermissions(GroupBean group, DocBean document) {
        this.group = group;
        this.document = document;
        this.id = new DocGroupPermissionsId(group.getId(), document.getId());
    }

    public DocGroupPermissions(GroupBean group, DocBean document, Permissions permissions) {
        this.group = group;
        this.document = document;
        this.id = new DocGroupPermissionsId(group.getId(), document.getId());
        this.permissions = permissions;
    }

    public DocGroupPermissionsId getId() {
        return id;
    }

    public void setId(DocGroupPermissionsId id) {
        this.id = id;
    }

    public GroupBean getGroup() {
        return group;
    }

    public void setGroup(GroupBean group) {
        this.group = group;
    }

    public DocBean getDocument() {
        return document;
    }

    public void setDocument(DocBean document) {
        this.document = document;
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
        DocGroupPermissions that = (DocGroupPermissions) o;
        return Objects.equals(group, that.group) && Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, document);
    }
}
