package com.nikitiuk.documentstoragewithframework.dao.implementations;

import com.nikitiuk.documentstoragewithframework.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.entities.FolderGroupPermissions;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.exceptions.AlreadyExistsException;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Bean
public class FolderDao /*extends GenericHibernateDao<FolderBean>*/ {

    private static final Logger logger = LoggerFactory.getLogger(FolderDao.class);

    /*public FolderDao() {
        super(FolderBean.class);
    }*/

    @AutoWire
    private HibernateUtil hibernateUtil;

    public FolderBean saveFolder(FolderBean folder) throws Exception {
        if (exists(folder)) {
            throw new AlreadyExistsException("Such Folder Already Exists");
        }
        return save(folder);
    }

    public FolderBean updateFolder(FolderBean updatedFolder) throws Exception {
        InspectorService.checkIfFolderIsNull(updatedFolder);
        return save(updatedFolder);
    }

    //@Override
    public FolderBean getById(Long folderId) {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FolderBean folderBean = session.get(FolderBean.class, folderId);
            if (folderBean != null) {
                initializeConnections(folderBean);
            }
            transaction.commit();
            session.close();
            return folderBean;
        } catch (Exception e) {
            logger.error("Error at FolderDao getFolderById.", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<FolderBean> getAllFolders() {
        Transaction transaction = null;
        List<FolderBean> folderBeanList = new ArrayList<>();
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderBeanList = session.createQuery("FROM FolderBean", FolderBean.class).list();
            if (CollectionUtils.isNotEmpty(folderBeanList)) {
                initializeConnectionsForList(folderBeanList);
            }
            transaction.commit();
            return folderBeanList;
        } catch (Exception e) {
            logger.error("Error at FolderDao getAll.", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return folderBeanList;
    }

    /*public FolderBean getFolderByPath(String path) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FolderBean folderBean = session.createQuery("FROM FolderBean WHERE path = '"
                    + path + "'", FolderBean.class).uniqueResult();
            if (folderBean != null) {
                initializeConnections(folderBean);
            }
            transaction.commit();
            session.close();
            return folderBean;
        } catch (Exception e) {
            logger.error("Error at FolderDao getFolderByPath.", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }*/

    public List<FolderBean> getFoldersForUser(UserBean userBean) {
        List<FolderBean> folderBeanList = new ArrayList<>();
        Hibernate.initialize(userBean.getGroups());
        if (userBean.getGroups() == null || userBean.getGroups().isEmpty()) {
            return folderBeanList;
        }
        List<Long> groupIds = userBean.getGroupsIds();
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderBeanList = session.createQuery("SELECT DISTINCT folder FROM FolderBean folder INNER JOIN FolderGroupPermissions permissions ON folder.id = permissions.folder.id " +
                    "WHERE permissions.group.id IN (:ids)", FolderBean.class).setParameterList("ids", groupIds).list();
            if (CollectionUtils.isNotEmpty(folderBeanList)) {
                initializeConnectionsForList(folderBeanList);
            }
            transaction.commit();
            return folderBeanList;
        } catch (Exception e) {
            logger.error("Error at FolderDao getFoldersForUser.", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void deleteFolder(Long folderId) {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM DocBean WHERE path LIKE CONCAT((SELECT path FROM FolderBean WHERE id = (:id)) ,'%')")
                    .setParameter("id", folderId).executeUpdate();
            /*session.createSQLQuery("DELETE FROM Documents WHERE document_path LIKE CONCAT((SELECT folder_path FROM Folders WHERE id = (:id)) ,'%')")
                    .setParameter("id", folderId).executeUpdate();*/
            session.createSQLQuery("DELETE FROM Folders WHERE folder_path LIKE CONCAT((SELECT folder_path FROM (SELECT * FROM Folders) AS X WHERE id = (:id)) ,'%')")
                    .setParameter("id", folderId).executeUpdate();
            /*session.createSQLQuery("DELETE FROM Folder_group_permissions WHERE folder_id = (:id)")
                    .setParameter("id", folderId).executeUpdate();*/
            /*session.createQuery("DELETE FROM FolderBean WHERE id = (:id)")
                    .setParameter("id", folderId).executeUpdate();*/
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at FolderDao delete.", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public boolean exists(FolderBean folder) throws Exception {
        Session session = hibernateUtil.getSessionFactory().openSession();
        return session.createQuery(
                "SELECT 1 FROM FolderBean WHERE EXISTS (SELECT 1 FROM FolderBean WHERE path = '" + folder.getPath() + "')")
                .uniqueResult() != null;
    }

    private FolderBean save(FolderBean folder) throws Exception {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession();) {
            /*folder.setFoldersPermissions(getPermissionsOnlyForExistingGroups(folder));*/
            transaction = session.beginTransaction();
            session.saveOrUpdate(folder);
            transaction.commit();
            return folder;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    /*private Set<FolderGroupPermissions> getPermissionsOnlyForExistingGroups(FolderBean folder) throws Exception {
        if (folder == null) {
            throw new Exception("No FolderBean was passed to check.");
        }
        Set<FolderGroupPermissions> folderGroupPermissionsToSet = new HashSet<>();
        Set<FolderGroupPermissions> folderGroupPermissionsToCheck = folder.getFoldersPermissions();
        if (CollectionUtils.isEmpty(folderGroupPermissionsToCheck)) {
            return folderGroupPermissionsToSet;
        }
        Map<String, Permissions> groupNamesAndPermissions = new HashMap<>();
        Map<Long, Permissions> groupIdsAndPermissions = new HashMap<>();
        for (FolderGroupPermissions folderGroupPermissions : folderGroupPermissionsToCheck) {
            if (folderGroupPermissions.getPermissions() != null) {
                if (folderGroupPermissions.getGroup().getId() != null) {
                    groupIdsAndPermissions.put(folderGroupPermissions.getGroup().getId(), folderGroupPermissions.getPermissions());
                } else if (folderGroupPermissions.getGroup().getName() != null) {
                    groupNamesAndPermissions.put(folderGroupPermissions.getGroup().getName(), folderGroupPermissions.getPermissions());
                }
            }
        }
        if (groupNamesAndPermissions.isEmpty() && groupIdsAndPermissions.isEmpty()) {
            return folderGroupPermissionsToSet;
        }
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            if (!groupNamesAndPermissions.isEmpty()) {
                Set<GroupBean> checkedGroupsWithNames = new HashSet<>(session.createQuery("FROM GroupBean WHERE name IN (:groupNames)", GroupBean.class)
                        .setParameterList("groupNames", groupNamesAndPermissions.keySet()).list());
                for (GroupBean groupBean : checkedGroupsWithNames) {
                    folderGroupPermissionsToSet.add(new FolderGroupPermissions(groupBean, folder, groupNamesAndPermissions.get(groupBean.getName())));
                }
            }
            if (!groupIdsAndPermissions.isEmpty()) {
                Set<GroupBean> checkedGroupsWithIds = new HashSet<>(session.createQuery("FROM GroupBean WHERE id IN (:ids)", GroupBean.class)
                        .setParameterList("ids", groupIdsAndPermissions.keySet()).list());
                for (GroupBean groupBean : checkedGroupsWithIds) {
                    folderGroupPermissionsToSet.add(new FolderGroupPermissions(groupBean, folder, groupIdsAndPermissions.get(groupBean.getId())));
                }
            }
            transaction.commit();
            return folderGroupPermissionsToSet;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }*/

    private void initializeConnectionsForList(List<FolderBean> folderBeanList) {
        for (FolderBean folderBean : folderBeanList) {
            initializeConnections(folderBean);
        }
    }

    private void initializeConnections(FolderBean folder) {
        for (FolderGroupPermissions folderGroupPermissions : folder.getFoldersPermissions()) {
            Hibernate.initialize(folderGroupPermissions);
        }
    }
}