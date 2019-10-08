package com.nikitiuk.documentstoragewithframework.utils;

import com.nikitiuk.documentstoragewithframework.dao.helpers.Populator;
import com.nikitiuk.documentstoragewithframework.dao.implementations.*;
import com.nikitiuk.documentstoragewithframework.entities.*;
import com.nikitiuk.documentstoragewithframework.entities.helpers.DocGroupPermissionsId;
import com.nikitiuk.documentstoragewithframework.entities.helpers.FolderGroupPermissionsId;
import com.nikitiuk.documentstoragewithframework.services.LocalStorageService;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;
    private static LocalStorageService localStorageService;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                // Hibernate settings equivalent to hibernate.cfg.xml's properties
                Properties settings = new Properties();
                /*settings.put(Environment.DRIVER, "org.h2.Driver");
                settings.put(Environment.URL, "jdbc:h2:mem:docstore;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS DOCSTORE");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
                settings.put(Environment.DEFAULT_SCHEMA, "docstore.docstore");*/
                settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
                settings.put(Environment.URL, "jdbc:mysql://localhost:3306/document_tracker?useSSL=false&serverTimezone=Europe/Moscow");
                settings.put(Environment.USER, "root");
                settings.put(Environment.PASS, "deranbor8989");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
                //settings.put(Environment.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
                //settings.put(Environment.SHOW_SQL, "true");
                settings.put(Environment.FORMAT_SQL, "true");
                //settings.put(Environment.USE_SQL_COMMENTS, "true");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.HBM2DDL_AUTO, "create");
                settings.put(Environment.AUTOCOMMIT, "false"); //default
                settings.put(Environment.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, "true");
                //settings.put(Environment.UNIQUE_CONSTRAINT_SCHEMA_UPDATE_STRATEGY, "RECREATE_QUIETLY");
                //settings.put(Environment.STORAGE_ENGINE, "");
                configuration.setProperties(settings);
                configuration.addAnnotatedClass(DocBean.class);
                configuration.addAnnotatedClass(FolderBean.class);
                configuration.addAnnotatedClass(GroupBean.class);
                configuration.addAnnotatedClass(UserBean.class);
                configuration.addAnnotatedClass(DocGroupPermissions.class);
                configuration.addAnnotatedClass(DocGroupPermissionsId.class);
                configuration.addAnnotatedClass(FolderGroupPermissions.class);
                configuration.addAnnotatedClass(FolderGroupPermissionsId.class);
                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                localStorageService = new LocalStorageService();
                populateTables();
            } catch (Exception e) {
                logger.error("Exception caught while creating Session Factory. ", e);
            }
        }
        return sessionFactory;
    }

    private static void populateTables() throws Exception {
        Populator.populateTableWithGroups();
        Populator.populateTableWithUsers();
        Populator.populateTableWithFolders(localStorageService.listFoldersInPath());
        Populator.populateTableWithDocs(localStorageService.listDocumentsInPath());
        Populator.populateTableWithFolderGroupPermissions();
        Populator.populateTableWithDocGroupPermissions();
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
