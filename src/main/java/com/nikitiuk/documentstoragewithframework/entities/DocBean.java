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
@Table(name = "Documents")
@NaturalIdCache
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
public class DocBean {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @Column(name = "document_name", nullable = false)
    private String name;

    @Column(name = "document_path", unique = true, nullable = false)
    private String path;

    @OneToMany(
            mappedBy = "document",
            cascade = CascadeType.ALL,
            orphanRemoval = true/*,
            fetch = FetchType.EAGER*/
    )
    //@Fetch(FetchMode.SUBSELECT)
    @OrderBy
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<DocGroupPermissions> documentsPermissions = new HashSet<>();

    public DocBean(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public DocBean() {

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<DocGroupPermissions> getDocumentsPermissions() {
        return documentsPermissions;
    }

    public void setDocumentsPermissions(Set<DocGroupPermissions> documentsPermissions) {
        this.documentsPermissions = documentsPermissions;
    }

    public void addGroup(GroupBean group, Permissions permissions) {
        if (checkIfDocumentHasGroup(group)) {
            return;
        }
        DocGroupPermissions docGroupPermissions = new DocGroupPermissions(group, this);
        docGroupPermissions.setPermissions(permissions);
        documentsPermissions.add(docGroupPermissions);
    }

    public boolean checkIfDocumentHasGroup(GroupBean group) {
        for (DocGroupPermissions docGroupPermissions : documentsPermissions) {
            if (docGroupPermissions.getDocument().equals(this) &&
                    docGroupPermissions.getGroup().equals(group)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Document [document_id=" + id + ", document_name=" + name + ", document_path=" + path + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocBean docBean = (DocBean) o;
        return Objects.equals(path, docBean.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
