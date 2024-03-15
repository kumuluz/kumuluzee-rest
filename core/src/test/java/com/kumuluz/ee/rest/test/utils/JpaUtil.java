package com.kumuluz.ee.rest.test.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * @author Tilen Faganel
 */
public class JpaUtil {

    private static JpaUtil instance;

    private final EntityManagerFactory emfEclipseLink;
    private final EntityManagerFactory emfHibernate;

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
