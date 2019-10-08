package com.nikitiuk.documentstoragewithframework.dao.implementations;

import com.nikitiuk.documentstoragewithframework.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.exceptions.AlreadyExistsException;
import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import javassist.NotFoundException;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Bean
public class UserDao extends GenericHibernateDao<UserBean> {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    public UserDao() {
        super(UserBean.class);
    }

    public List<UserBean> getUsers() {
        Transaction transaction = null;
        List<UserBean> userBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            userBeanList = session.createQuery("FROM UserBean", UserBean.class).list();
            for (UserBean userBean : userBeanList) {
                Hibernate.initialize(userBean.getGroups());
            }
            transaction.commit();
            return userBeanList;
        } catch (Exception e) {
            logger.error("Error at UserDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            return userBeanList;
        }
    }

    @Override
    public UserBean getById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserBean userBean = session.get(UserBean.class, id);
            if(userBean != null) {
                Hibernate.initialize(userBean.getGroups());
            }
            transaction.commit();
            session.close();
            return userBean;
        } catch (Exception e) {
            logger.error("Error at UserDao getById: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public UserBean getUserByName(String userName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserBean userBean = session.createQuery("FROM UserBean WHERE name = '"
                    + userName + "'", UserBean.class).uniqueResult();
            if(userBean != null) {
                Hibernate.initialize(userBean.getGroups());
                /*for(GroupBean groupBean : userBean.getGroups()){
                    Hibernate.initialize(groupBean.getDocumentsPermissions());
                    Hibernate.initialize(groupBean.getFoldersPermissions());
                }*/
            }
            transaction.commit();
            session.close();
            return userBean;
        } catch (Exception e) {
            logger.error("Error at UserDao getUserByName: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public UserBean saveUser(UserBean userBean) throws Exception{
        try {
            if (exists(userBean)) {
                throw new AlreadyExistsException("Such user already exists.");
            }
            userBean.setId(null);
            return save(userBean);
        } catch (Exception e) {
            logger.error("Error at UserDao saveUser: ", e);
            throw e;
        }
    }

    public UserBean updateUser(UserBean userBean) throws Exception{
        try {
            UserBean updatedUser = getUserByName(userBean.getName());
            if (updatedUser == null) {
                throw new NotFoundException("User not found.");
            }
            updatedUser.setGroups(userBean.getGroups());
            updatedUser.setPassword(userBean.getPassword());
            Hibernate.initialize(updatedUser.getGroups());
            return save(updatedUser);
        } catch (Exception e) {
            logger.error("Error at UserDao updateAndSaveUser: ", e);
            throw e;
        }
    }

    public void deleteUserByName(String userName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM UserBean WHERE name = '"
                    + userName + "'").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at UserDao deleteUserByName: ", e);
        }
    }

    public boolean exists(UserBean user) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        return session.createQuery(
                "SELECT 1 FROM UserBean WHERE EXISTS (SELECT 1 FROM UserBean WHERE name = '"+ user.getName() +"')")
                .uniqueResult() != null;
    }

    private UserBean save(UserBean user) throws Exception {
        Transaction transaction = null;
        try {
            user.setGroups(checkGroupsAndReturnMatched(user));
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(user);
            Hibernate.initialize(user.getGroups());
            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private Set<GroupBean> checkGroupsAndReturnMatched(UserBean user) throws Exception {
        if (user == null) {
            throw new Exception("No UserBean was passed to check.");
        }
        Set<GroupBean> checkedGroups = new HashSet<>();
        if (user.getGroups() == null || user.getGroups().isEmpty()){
            return checkedGroups;
        }
        Transaction transaction = null;
        try{
            Session session = HibernateUtil.getSessionFactory().openSession();
            Set<String> groupNames = new HashSet<>();
            for(GroupBean groupBean : user.getGroups()){
                groupNames.add(groupBean.getName());
            }
            transaction = session.beginTransaction();
            checkedGroups.addAll(session.createQuery("FROM GroupBean WHERE name IN (:groupNames)", GroupBean.class).setParameterList("groupNames", groupNames).list());
            transaction.commit();
            return checkedGroups;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}