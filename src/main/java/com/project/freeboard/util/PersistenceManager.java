package com.project.freeboard.util;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public final class PersistenceManager {

	private static EntityManagerFactory entityManagerFactory = null;

	public static EntityManager getEntityManager() {

		if (entityManagerFactory == null)
			entityManagerFactory = Persistence.createEntityManagerFactory("freeboard");

		return entityManagerFactory.createEntityManager();
	}

}