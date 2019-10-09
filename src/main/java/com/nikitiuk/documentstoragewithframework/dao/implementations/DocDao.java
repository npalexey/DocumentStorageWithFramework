package com.nikitiuk.documentstoragewithframework.dao.implementations;

import com.nikitiuk.documentstoragewithframework.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithframework.entities.DocBean;
import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.exceptions.AlreadyExistsException;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Bean
public class DocDao /*extends GenericHibernateDao<DocBean>*/ {

    private static final Logger logger = LoggerFactory.getLogger(DocDao.class);

    /*public DocDao() {
        super(DocBean.class);
    }*/

    @AutoWire
    private HibernateUtil hibernateUtil;

    public DocBean saveDocument(DocBean document) throws Exception {
        if (exists(document)) {
            throw new AlreadyExistsException("Such Document Already Exists");
        }
        return save(document);
    }

    public DocBean updateDocument(DocBean document) throws Exception {
        InspectorService.checkIfDocumentIsNull(document);
        return save(document);
    }


    public DocBean getById(Long documentId) {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocBean docBean = session.createQuery("FROM DocBean doc JOIN FETCH doc.documentsPermissions WHERE doc.id = :id", DocBean.class)
                    .setParameter("id", documentId).uniqueResult();
            transaction.commit();
            session.close();
            return docBean;
        } catch (Exception e) {
            logger.error("Error at DocDao getDocById: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<DocBean> getAllDocuments() throws Exception {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<DocBean> docBeanList = session.createQuery("FROM DocBean doc LEFT JOIN FETCH doc.documentsPermissions", DocBean.class).list();
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            logger.error("Error at DocDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<DocBean> getDocumentsForUser(UserBean userBean) throws Exception {
        List<DocBean> docBeanList = new ArrayList<>();
        if (CollectionUtils.isEmpty(userBean.getGroups())) {
            return docBeanList;
        }
        List<Long> groupIds = userBean.getGroupsIds();
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("SELECT DISTINCT doc FROM DocBean doc JOIN FETCH doc.documentsPermissions permissions " +
                    "WHERE permissions.group.id IN (:ids)", DocBean.class).setParameterList("ids", groupIds)
                    .setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false).list();
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            logger.error("Error at DocDao getDocumentsForUser: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<DocBean> getDocumentsForUserInFolder(UserBean userBean, FolderBean folderBean) throws Exception {
        List<DocBean> docBeanList = new ArrayList<>();
        if (folderBean == null || folderBean.getPath() == null
                || CollectionUtils.isEmpty(userBean.getGroups())) {
            return docBeanList;
        }
        List<Long> groupIds = userBean.getGroupsIds();
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("SELECT DISTINCT doc FROM DocBean doc JOIN FETCH doc.documentsPermissions permissions " +
                    "WHERE permissions.group.id IN (:ids) AND doc.path LIKE '" + folderBean.getPath() + "%'", DocBean.class)
                    .setParameterList("ids", groupIds).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false).list();
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            logger.error("Error at DocDao getDocumentsForUser: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }

    }

    public void deleteDocument(Long documentId) throws Exception {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM DocBean WHERE id = (:id)")
                    .setParameter("id", documentId).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocDao delete: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public boolean exists(DocBean document) throws Exception {
        Session session = hibernateUtil.getSessionFactory().openSession();
        return session.createQuery(
                "SELECT 1 FROM DocBean WHERE EXISTS (SELECT 1 FROM DocBean WHERE path = '" + document.getPath() + "')")
                .uniqueResult() != null;
    }

    private DocBean save(DocBean document) throws Exception {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(document);
            transaction.commit();
            return document;
        } catch (Exception e) {
            logger.error("Error at DocDao save.", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void refresh(DocBean document) {
        Transaction transaction = null;
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.refresh(document);
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocDao refresh.", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}