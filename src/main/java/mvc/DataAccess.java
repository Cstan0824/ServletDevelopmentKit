package mvc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DataAccess {
    private static EntityManagerFactory entityManagerFactory = Persistence
            .createEntityManagerFactory("myPersistenceUnit");

    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public static boolean isConnected() {
        return entityManagerFactory.isOpen();
    }

    public static void closeFactory() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
