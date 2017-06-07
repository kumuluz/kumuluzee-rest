package com.kumuluz.ee.rest.test.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Tilen Faganel
 */
public class JpaUtil {

    private static JpaUtil instance;

    private EntityManagerFactory emfEclipseLink;
    private EntityManagerFactory emfHibernate;

    public static JpaUtil getInstance() {
        if (instance == null) {
            instance = new JpaUtil();
        }

        return instance;
    }

    private JpaUtil() {

        emfEclipseLink = Persistence.createEntityManagerFactory("kumuluzee-rest-eclipselink");
        emfHibernate = Persistence.createEntityManagerFactory("kumuluzee-rest-hibernate");
    }

    public EntityManager getEclipselinkEntityManager() {
        return emfEclipseLink.createEntityManager();
    }

    public EntityManager getHibernateEntityManager() {
        return emfHibernate.createEntityManager();
    }
}
