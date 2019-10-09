package com.nikitiuk.documentstoragewithframework.dao.helpers;

import com.nikitiuk.documentstoragewithframework.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithframework.dao.implementations.FolderDao;
import com.nikitiuk.documentstoragewithframework.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithframework.entities.*;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Bean
public class Populator {

    private static final Logger logger = LoggerFactory.getLogger(Populator.class);

    private static List<GroupBean> groupList = new ArrayList<>(Arrays.asList(new GroupBean("ADMINS"),
            new GroupBean("USERS"), new GroupBean("GUESTS")));
    private static List<UserBean> userList = new ArrayList<>();
    private static List<FolderGroupPermissions> folderGroupPermissionsList = new ArrayList<>();
    private static List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>();

    @AutoWire
    private DocDao docDao;
    @AutoWire
    private GroupDao groupDao;
    @AutoWire
    private FolderDao folderDao;
    @AutoWire
    private HibernateUtil hibernateUtil;

    private void getUserListForPopulate() {
        userList.add(new UserBean("Admin", "adminpswrd"));
        userList.add(new UserBean("Employee", "employeepswrd"));
        userList.add(new UserBean("Guest", "guestpswrd"));
        Set<GroupBean> groupBeans0 = new HashSet<>(Collections.singletonList(groupList.get(2)));
        userList.get(2).setGroups(groupBeans0);
        Set<GroupBean> groupBeans1 = new HashSet<>(Arrays.asList(groupList.get(2), groupList.get(1)));
        userList.get(1).setGroups(groupBeans1);
        Set<GroupBean> groupBeans2 = new HashSet<>(Arrays.asList(groupList.get(2), groupList.get(1),
                groupList.get(0)));
        userList.get(0).setGroups(groupBeans2);
    }

    private void getFolderGroupPermissionsListForPopulate() {
        try {
            List<FolderBean> folderBeanList = folderDao.getAllFolders();
            if (CollectionUtils.isNotEmpty(folderBeanList)) {
                List<GroupBean> groupBeanList = groupDao.getGroups();
                for (FolderBean folderBean : folderBeanList) {
                    FolderGroupPermissions folderGroupPermissionsAdmin = new FolderGroupPermissions(groupBeanList.get(0), folderBean);
                    FolderGroupPermissions folderGroupPermissionsUser = new FolderGroupPermissions(groupBeanList.get(1), folderBean);
                    FolderGroupPermissions folderGroupPermissionsGuest = new FolderGroupPermissions(groupBeanList.get(2), folderBean);
                    folderGroupPermissionsAdmin.setPermissions(Permissions.WRITE);
                    folderGroupPermissionsUser.setPermissions(Permissions.READ);
                    folderGroupPermissionsGuest.setPermissions(Permissions.READ);
                    folderGroupPermissionsList.add(folderGroupPermissionsAdmin);
                    folderGroupPermissionsList.add(folderGroupPermissionsUser);
                    folderGroupPermissionsList.add(folderGroupPermissionsGuest);
                }
            }
        } catch (Exception e) {
            logger.error("Error at Populator getFolderGroupPermissionsListForPopulate.", e);
        }
    }

    private void getDocGroupPermissionsListForPopulate() {
        try {
            List<DocBean> docBeanList = docDao.getAllDocuments();
            if (CollectionUtils.isNotEmpty(docBeanList)) {
                List<GroupBean> groupBeanList = groupDao.getGroups();
                for (DocBean docBean : docBeanList) {
                    DocGroupPermissions docGroupPermissionsAdmin = new DocGroupPermissions(groupBeanList.get(0), docBean);
                    DocGroupPermissions docGroupPermissionsUser = new DocGroupPermissions(groupBeanList.get(1), docBean);
                    DocGroupPermissions docGroupPermissionsGuest = new DocGroupPermissions(groupBeanList.get(2), docBean);
                    docGroupPermissionsAdmin.setPermissions(Permissions.WRITE);
                    docGroupPermissionsUser.setPermissions(Permissions.READ);
                    docGroupPermissionsGuest.setPermissions(Permissions.READ);
                    docGroupPermissionsList.add(docGroupPermissionsAdmin);
                    docGroupPermissionsList.add(docGroupPermissionsUser);
                    docGroupPermissionsList.add(docGroupPermissionsGuest);
                }
            }
        } catch (Exception e) {
            logger.error("Error at Populator getDocGroupPermissionsListForPopulate.", e);
        }
    }

    public void populateTableWithGroups() {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            for (GroupBean groupBean : groupList) {
                transaction = session.beginTransaction();
                session.saveOrUpdate(groupBean);
                transaction.commit();
            }
        } catch (Exception e) {
            logger.error("Error at Populator populateTableWithGroups.", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void populateTableWithUsers() {
        getUserListForPopulate();
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            for (UserBean userBean : userList) {
                transaction = session.beginTransaction();
                session.saveOrUpdate(userBean);
                Hibernate.initialize(userBean.getGroups());
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at Populator populateTableWithUsers.", e);
        }
    }

    public void populateTableWithFolders(List<FolderBean> folderBeanList) {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            for (FolderBean folderBean : folderBeanList) {
                transaction = session.beginTransaction();
                session.saveOrUpdate(folderBean);
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at Populator populateTableWithFolders.", e);
        }
    }

    public void populateTableWithDocs(List<DocBean> docBeanList) {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            for (DocBean docBean : docBeanList) {
                transaction = session.beginTransaction();
                session.saveOrUpdate(docBean);
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at Populator populateTableWithDocs.", e);
        }
    }

    public void populateTableWithFolderGroupPermissions() {
        getFolderGroupPermissionsListForPopulate();
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            for (FolderGroupPermissions folderGroupPermissions : folderGroupPermissionsList) {
                transaction = session.beginTransaction();
                session.saveOrUpdate(folderGroupPermissions);
                transaction.commit();
            }
        } catch (Exception e) {
            logger.error("Error at Populator populateTableWithFolderGroupPermissions.", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void populateTableWithDocGroupPermissions() {
        getDocGroupPermissionsListForPopulate();
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            for (DocGroupPermissions docGroupPermissions : docGroupPermissionsList) {
                transaction = session.beginTransaction();
                session.saveOrUpdate(docGroupPermissions);
                transaction.commit();
            }
        } catch (Exception e) {
            logger.error("Error at Populator populateTableWithDocGroupPermissions.", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }
}
