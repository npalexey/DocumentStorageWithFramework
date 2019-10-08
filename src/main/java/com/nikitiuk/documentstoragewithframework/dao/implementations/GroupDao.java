package com.nikitiuk.documentstoragewithframework.dao.implementations;

import com.nikitiuk.documentstoragewithframework.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.exceptions.AlreadyExistsException;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupDao extends GenericHibernateDao<GroupBean> {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    public GroupDao() {
        super(GroupBean.class);
    }

    public List<GroupBean> getGroups() throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<GroupBean> groupBeanList = session.createQuery("FROM GroupBean", GroupBean.class).list();
            if (CollectionUtils.isNotEmpty(groupBeanList)) {
                initializeConnectionsForList(groupBeanList);
            }
            transaction.commit();
            return groupBeanList;
        } catch (Exception e) {
            logger.error("Error at GroupDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public GroupBean getGroupByName(String groupName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            GroupBean groupBean = session.createQuery("FROM GroupBean WHERE name = '"
                    + groupName + "'", GroupBean.class).uniqueResult();
            if (groupBean != null) {
                initializeConnections(groupBean);
            }
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
            logger.error("Error at GroupDao getGroupByName: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public GroupBean getById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            GroupBean groupBean = session.get(GroupBean.class, id);
            initializeConnections(groupBean);
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
            logger.error("Error at GroupDao getById: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public GroupBean saveGroup(GroupBean groupBean) throws Exception {
        try {
            if (exists(groupBean)) {
                throw new AlreadyExistsException("Such Group Already Exists.");
            }
            boolean requiresMerge = true;
            groupBean.setId(null);
            return save(groupBean, requiresMerge);
        } catch (Exception e) {
            logger.error("Error at GroupDao saveGroup: ", e);
            throw e;
        }
    }

    public GroupBean updateGroup(GroupBean groupBean) throws Exception {
        boolean requiresMerge = true;
        GroupBean updatedGroup = getGroupByName(groupBean.getName());
        InspectorService.checkIfGroupIsNull(updatedGroup);
        if (updatedGroup.getUsers().containsAll(groupBean.getUsers())) {
            requiresMerge = false;
        }
        updatedGroup.setUsers(groupBean.getUsers());
        initializeConnections(updatedGroup);
        return save(updatedGroup, requiresMerge);
    }

    public boolean exists(GroupBean group) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        return session.createQuery(
                "SELECT 1 FROM GroupBean WHERE EXISTS (SELECT 1 FROM GroupBean WHERE name = '" + group.getName() + "')")
                .uniqueResult() != null;
    }

    private GroupBean save(GroupBean group, boolean requiresMerge) throws Exception {
        Transaction transaction = null;
        try {
            group.setUsers(getExistingUsers(group));
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(group);
            if (requiresMerge) {
                session.merge(group);
            }
            transaction.commit();
            return group;
        } catch (Exception e) {
            logger.error("Error at GroupDao save.", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private Set<UserBean> getExistingUsers(GroupBean group) throws Exception {
        InspectorService.checkIfGroupIsNull(group);
        Set<UserBean> checkedUsers = new HashSet<>();
        if (CollectionUtils.isEmpty(group.getUsers())) {
            return checkedUsers;
        }
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Set<String> userNames = group.getUserNamesSet();
            transaction = session.beginTransaction();
            checkedUsers.addAll(session.createQuery("FROM UserBean WHERE name IN (:userNames)", UserBean.class).setParameterList("userNames", userNames).list());
            transaction.commit();
            return checkedUsers;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private void initializeConnectionsForList(List<GroupBean> groupBeanList) {
        for (GroupBean groupBean : groupBeanList) {
            initializeConnections(groupBean);
        }
    }

    private void initializeConnections(GroupBean group) {
        Hibernate.initialize(group.getUsers());
    }
}