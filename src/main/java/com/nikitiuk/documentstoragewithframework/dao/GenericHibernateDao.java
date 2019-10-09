package com.nikitiuk.documentstoragewithframework.dao;

import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public class GenericHibernateDao<T> {

    private static final Logger logger = LoggerFactory.getLogger(GenericHibernateDao.class);

    private HibernateUtil hibernateUtil;

    private Class<T> clazz;
    //private SessionFactory sessionFactory;

    public GenericHibernateDao(Class<T> clazz) {
        //this.sessionFactory = HibernateUtil.getSessionFactory();
        this.clazz = clazz;
    }

    private Session getCurrentSession() {
        return hibernateUtil.getSessionFactory().getCurrentSession();
    }


    private <E> E executeFunction(Function<Session, E> function) {
        Transaction transaction = null;
        try (Session session = getCurrentSession()) {
            transaction = session.beginTransaction();
            return function.apply(session);
        } catch (Exception e) {
            logger.error("Error while executing function. ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            return null;
        }
    }

    public T getById(Long id) {
        return executeFunction((session) -> session.get(clazz, id));
    }

    public List getAll() {
        return executeFunction((session) -> session.createQuery("FROM " + clazz.getSimpleName(), clazz).list());
    }

    public void create(T entity) {
        executeFunction((session) -> {
            List<T> beanList = session.createQuery("FROM " + clazz.getSimpleName(), clazz).list();
            if (!beanList.isEmpty()) {
                for (T bean : beanList) {
                    if (!bean.equals(entity)) {
                        session.saveOrUpdate(entity);
                        session.getTransaction().commit();
                    }
                }
            }
            return entity;
        });
    }

    /*public T update(T entity) {
        return (T) getCurrentSession().merge(entity);
    }*/

    public void refresh(T entity) {
        executeFunction((session) -> {
            session.refresh(entity);
            return entity;
        });
    }

    public void deleteById(Long entityId) {
        executeFunction((session) -> {
            T entity = session.load(clazz, entityId);
            session.delete(entity);
            session.getTransaction().commit();
            return entityId;
        });
    }
}
