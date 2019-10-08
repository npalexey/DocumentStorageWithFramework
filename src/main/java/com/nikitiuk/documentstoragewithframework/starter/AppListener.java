package com.nikitiuk.documentstoragewithframework.starter;

import com.nikitiuk.documentstoragewithframework.utils.HibernateUtil;
import com.nikitiuk.javabeansinitializer.annotations.ApplicationCustomContext;
import com.nikitiuk.javabeansinitializer.annotations.ContextInitializer;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.listener.ApplicationListener;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.listener.ContextInitialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

@ApplicationListener
public class AppListener {

    private static final Logger logger = LoggerFactory.getLogger(AppListener.class);
    private static ApplicationCustomContext applicationCustomContext = null;

    public static ApplicationCustomContext getContext() {
        if (applicationCustomContext == null) {
            return applicationCustomContext = new ContextInitializer().initializeContext("com.nikitiuk.documentstoragewithsearchcapability");
        }
        return applicationCustomContext;
    }

    @ContextInitialized
    public void contextInitialized() {
        try {
            Properties prop = new Properties();
            String propFileName = "appconfig.properties";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException(String.format("Property file '%s' is not found in the classpath", propFileName));
            }
            System.setProperty("local.path.to.storage", prop.getProperty("LOCAL_PATH"));
            System.setProperty("current.authorization.property", prop.getProperty("AUTHORIZATION_PROPERTY"));
            System.setProperty("current.authentication.scheme", prop.getProperty("AUTHENTICATION_SCHEME"));
            System.setProperty("tika.config", prop.getProperty("TIKA_CONFIG"));
            System.setProperty("default.folder", prop.getProperty("DEFAULT_FOLDER"));
            inputStream.close();
            HibernateUtil.getSessionFactory();
            applicationCustomContext = new ContextInitializer().initializeContext("com.nikitiuk.documentstoragewithframework");
        } catch (Exception e) {
            logger.error("Error at ApplicationListener contextInitialized: ", e);
        }
    }
}