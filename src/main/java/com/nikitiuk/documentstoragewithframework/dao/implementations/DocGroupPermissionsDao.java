package com.nikitiuk.documentstoragewithframework.dao.implementations;

import com.nikitiuk.documentstoragewithframework.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithframework.entities.DocBean;
import com.nikitiuk.documentstoragewithframework.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DocGroupPermissionsDao extends GenericHibernateDao<DocGroupPermissions> {

    private static final Logger logger = LoggerFactory.getLogger(DocGroupPermissionsDao.class);

    public DocGroupPermissionsDao() {
        super(DocGroupPermissions.class);
    }

    public List<DocGroupPermissions> getAllDocGroupPermissions() throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            List<DocGroupPermissions> docGroupPermissionsList = session.createQuery("FROM DocGroupPermissions per LEFT JOIN FETCH " +
                    "per.document LEFT JOIN FETCH per.group", DocGroupPermissions.class).list();
            transaction.commit();
            return docGroupPermissionsList;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public List<DocGroupPermissions> getPermissionsForDocumentsForGroup(Long groupId) throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            List<DocGroupPermissions> docGroupPermissionsList = session.createQuery("FROM DocGroupPermissions per LEFT JOIN FETCH " +
                    "per.document JOIN FETCH per.group WHERE per.group.id = :groupId", DocGroupPermissions.class)
                    .setParameter("groupId", groupId).list();
            transaction.commit();
            return docGroupPermissionsList;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public List<DocGroupPermissions> getPermissionsForDocument(Long documentId) throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            List<DocGroupPermissions> docPermissionsList = session.createQuery("FROM DocGroupPermissions per LEFT JOIN FETCH " +
                    "per.group JOIN FETCH per.document WHERE per.document.id = :documentId", DocGroupPermissions.class)
                    .setParameter("documentId", documentId).list();
            transaction.commit();
            return docPermissionsList;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public Integer deleteAllPermissionsForGroup(Long groupId) throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            Integer quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions " +
                    "WHERE group.id = :groupId").setParameter("groupId", groupId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public Integer deletePermissionsForDocumentForGroup(Long documentId, Long groupId) throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            Integer quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions " +
                    "WHERE group.id = :groupId AND document.id = :documentId")
                    .setParameter("groupId", groupId).setParameter("documentId", documentId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public Integer deleteAllPermissionsForDocumentExceptAdmin(Long documentId) throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            Integer quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions " +
                    "WHERE group.id != 1 AND document.id = :documentId")
                    .setParameter("documentId", documentId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public Integer deleteAllPermissionsForDocument(Long documentId) throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            Integer quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions " +
                    "WHERE document.id = :documentId").setParameter("documentId", documentId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    public DocGroupPermissions setWriteForDocumentForGroup(DocBean docBean, GroupBean groupBean) throws Exception {
        return setPermission(docBean.getId(), groupBean.getId(), Permissions.WRITE);
    }

    public DocGroupPermissions setWriteForDocumentForGroup(long documentId, long groupId) throws Exception {
        return setPermission(documentId, groupId, Permissions.WRITE);
    }

    public DocGroupPermissions setReadForDocumentForGroup(DocBean docBean, GroupBean groupBean) throws Exception {
        return setPermission(docBean.getId(), groupBean.getId(), Permissions.READ);
    }

    public DocGroupPermissions setReadForDocumentForGroup(long documentId, long groupId) throws Exception {
        return setPermission(documentId, groupId, Permissions.READ);
    }

    private DocGroupPermissions setPermission(Long documentId, Long groupId, Permissions permission) throws Exception {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            DocGroupPermissions docGroupPermissions = session.createQuery("FROM DocGroupPermissions " +
                    "WHERE document.id = :documentId AND group.id = :groupId", DocGroupPermissions.class)
                    .setParameter("documentId", documentId).setParameter("groupId", groupId).uniqueResult();
            DocGroupPermissions setDocGroupPermissions;
            if (docGroupPermissions != null) {
                docGroupPermissions.setPermissions(permission);
                session.merge(docGroupPermissions);
                setDocGroupPermissions = docGroupPermissions;
            } else {
                DocBean docBean = session.get(DocBean.class, documentId);
                InspectorService.checkIfDocumentIsNull(docBean);
                GroupBean groupBean = session.get(GroupBean.class, groupId);
                InspectorService.checkIfGroupIsNull(groupBean);
                setDocGroupPermissions = createNewPermissions(docBean, groupBean, permission);
                session.saveOrUpdate(setDocGroupPermissions);
                session.merge(docBean);
            }
            transaction.commit();
            return setDocGroupPermissions;
        } catch (Exception e) {
            if (transaction != null) {
                rollBackTransaction(transaction);
            }
            throw e;
        } finally {
            session.close();
        }
    }

    private DocGroupPermissions createNewPermissions(DocBean docBean, GroupBean groupBean, Permissions permission) {
        DocGroupPermissions newDocGroupPermissions = new DocGroupPermissions(groupBean, docBean);
        newDocGroupPermissions.setPermissions(permission);
        docBean.addGroup(groupBean, permission);
        return newDocGroupPermissions;
    }

    private void rollBackTransaction(Transaction transaction) {
        try {
            transaction.rollback();
        } catch (Exception txE) {
            logger.error("Error on transaction rollback.", txE);
        }
    }

    /*private void initializeConnectionsForList(List<DocGroupPermissions> docGroupPermissionsList) {
        for (DocGroupPermissions docGroupPermissions : docGroupPermissionsList) {
            initializeConnections(docGroupPermissions);
        }
    }

    private void initializeConnections(DocGroupPermissions docGroupPermissions) {
        Hibernate.initialize(docGroupPermissions.getDocument());
        Hibernate.initialize(docGroupPermissions.getGroup());
    }*/
}