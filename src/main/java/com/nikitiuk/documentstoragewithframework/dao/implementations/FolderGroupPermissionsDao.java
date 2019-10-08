package com.nikitiuk.documentstoragewithframework.dao.implementations;

import com.nikitiuk.documentstoragewithframework.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.entities.FolderGroupPermissions;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FolderGroupPermissionsDao extends GenericHibernateDao<FolderGroupPermissions> {

    private static final Logger logger = LoggerFactory.getLogger(FolderGroupPermissionsDao.class);

    public FolderGroupPermissionsDao() {
        super(FolderGroupPermissions.class);
    }

    public List<FolderGroupPermissions> getAllFolderGroupPermissions() {
        Transaction transaction = null;
        List<FolderGroupPermissions> folderGroupPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderGroupPermissionsList = session.createQuery("FROM FolderGroupPermissions", FolderGroupPermissions.class).list();
            if (CollectionUtils.isNotEmpty(folderGroupPermissionsList)) {
                initializeList(folderGroupPermissionsList);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at FolderGroupPermissionsDao getAllFolderGroupPermissions: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return folderGroupPermissionsList;
    }

    public List<FolderGroupPermissions> getPermissionsForFoldersForGroup(Long groupId) {
        Transaction transaction = null;
        List<FolderGroupPermissions> folderGroupPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderGroupPermissionsList = session.createQuery("FROM FolderGroupPermissions WHERE group = " + groupId, FolderGroupPermissions.class).list();
            if (CollectionUtils.isNotEmpty(folderGroupPermissionsList)) {
                initializeList(folderGroupPermissionsList);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at FolderGroupPermissionsDao getPermissionsForFoldersForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return folderGroupPermissionsList;
    }

    public List<FolderGroupPermissions> getPermissionsForFolder(Long folderId) {
        Transaction transaction = null;
        List<FolderGroupPermissions> folderPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderPermissionsList = session.createQuery("FROM FolderGroupPermissions WHERE folder = " + folderId, FolderGroupPermissions.class).list();
            if (CollectionUtils.isNotEmpty(folderPermissionsList)) {
                initializeList(folderPermissionsList);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at FolderGroupPermissionsDao getPermissionsForFolder: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return folderPermissionsList;
    }

    public Integer deleteAllPermissionsForGroup(Long groupId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM FolderGroupPermissions WHERE group = "
                    + groupId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at FolderGroupPermissionsDao deleteAllPermissionsForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Integer deletePermissionsForFolderForGroup(Long folderId, Long groupId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM FolderGroupPermissions WHERE group = "
                    + groupId + " AND folder = " + folderId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deletePermissionsForFolderForGroup: ", e);
            if (transaction != null && (transaction.getStatus() == TransactionStatus.FAILED_COMMIT || transaction.getStatus() == TransactionStatus.COMMITTING)) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Integer deleteAllPermissionsForFolderExceptAdmin(Long folderId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM FolderGroupPermissions WHERE group != 1 AND folder = "
                    + folderId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at FolderGroupPermissionsDao deleteAllPermissionsForFolderExceptAdmin: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Integer deleteAllPermissionsForFolder(Long folderId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM FolderGroupPermissions WHERE folder = "
                    + folderId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at FolderGroupPermissionsDao deleteAllPermissionsForFolder: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public FolderGroupPermissions setWriteForFolderForGroup(FolderBean folderBean, GroupBean groupBean) throws Exception {
        return setPermission(folderBean.getId(), groupBean.getId(), Permissions.WRITE);
    }

    public FolderGroupPermissions setWriteForFolderForGroup(long folderId, long groupId) throws Exception {
        return setPermission(folderId, groupId, Permissions.WRITE);
    }

    public FolderGroupPermissions setReadForFolderForGroup(FolderBean folderBean, GroupBean groupBean) throws Exception {
        return setPermission(folderBean.getId(), groupBean.getId(), Permissions.READ);
    }

    public FolderGroupPermissions setReadForFolderForGroup(long folderId, long groupId) throws Exception {
        return setPermission(folderId, groupId, Permissions.READ);
    }

    private FolderGroupPermissions setPermission(Long folderId, Long groupId, Permissions permission) throws Exception {
        Transaction transaction = null;
        FolderGroupPermissions setFolderGroupPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FolderGroupPermissions folderGroupPermissions = session.createQuery("FROM FolderGroupPermissions WHERE folder = "
                    + folderId + " AND group = " + groupId, FolderGroupPermissions.class).uniqueResult();
            if (folderGroupPermissions != null) {
                folderGroupPermissions.setPermissions(permission);
                session.merge(folderGroupPermissions);
                setFolderGroupPermissions = folderGroupPermissions;
            } else {
                FolderBean folderBean = session.load(FolderBean.class, folderId);
                InspectorService.checkIfFolderIsNull(folderBean);
                GroupBean groupBean = session.load(GroupBean.class, groupId);
                InspectorService.checkIfGroupIsNull(groupBean);
                setFolderGroupPermissions = createNewPermissions(folderBean, groupBean, permission);
                session.saveOrUpdate(setFolderGroupPermissions);
                session.merge(folderBean);
            }
            transaction.commit();
            return setFolderGroupPermissions;
        } catch (Exception e) {
            logger.error("Error at FolderGroupPermissionsDao set " + permission + " ForFolderForGroup, where " +
                    "FolderId = " + folderId + " and GroupId = " + groupId + ": ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private FolderGroupPermissions createNewPermissions(FolderBean folderBean, GroupBean groupBean, Permissions permission) {
        FolderGroupPermissions newFolderGroupPermissions = new FolderGroupPermissions(groupBean, folderBean);
        newFolderGroupPermissions.setPermissions(permission);
        /*groupBean.addFolder(folderBean, permission);*/
        folderBean.addGroup(groupBean, permission);
        return newFolderGroupPermissions;
    }

    private void initializeList(List<FolderGroupPermissions> folderGroupPermissionsList) {
        for (FolderGroupPermissions folderGroupPermissions : folderGroupPermissionsList) {
            initializeConnections(folderGroupPermissions);
        }
    }

    private void initializeConnections(FolderGroupPermissions folderGroupPermissions) {
        Hibernate.initialize(folderGroupPermissions.getFolder());
        Hibernate.initialize(folderGroupPermissions.getGroup());
    }
}