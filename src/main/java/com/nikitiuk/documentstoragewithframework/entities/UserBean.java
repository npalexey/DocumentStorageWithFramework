package com.nikitiuk.documentstoragewithframework.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.security.Principal;
import java.util.*;

@Entity
@Table(name = "Users")
@NaturalIdCache
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
public class UserBean implements Principal {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_name", unique = true, nullable = false)
    private String name;

    @Column(name = "user_password")
    private String password;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    //@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @OrderBy("id DESC")
    @JoinTable(
            name = "User_groups_binding",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "group_id") }
    )
    private Set<GroupBean> groups = new HashSet<>();

    public UserBean(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public UserBean() {

    }

    public Set<GroupBean> getGroups() {
        return groups;
    }

    public void setGroups(Set<GroupBean> groups) {
        this.groups = groups;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public List<Long> getGroupsIds() {
        List<Long> groupIds = new ArrayList<>();
        for (GroupBean groupBean : this.getGroups()) {
            groupIds.add(groupBean.getId());
        }
        return groupIds;
    }

    @Override
    public String   toString() {
        List<String> groupNames = new ArrayList<>();
        for(GroupBean groupBean : groups){
            groupNames.add(groupBean.getName());
        }
        return "User [user_id=" + id + ", user_name=" + name + ", user_groups=" + groupNames + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserBean userBean = (UserBean) o;
        return Objects.equals(name, userBean.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}