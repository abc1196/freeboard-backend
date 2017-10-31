package com.project.freeboard.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.project.freeboard.entity.Auctions;
import com.project.freeboard.util.PersistenceManager;

public class AuctionsDAO {

	private EntityManager em;

	public AuctionsDAO(EntityManager em) {
		this.em = em;
	}

	public boolean addAuctions(Auctions e) {
		// Check for already exists
		try {
			em.getTransaction().begin();
			em.persist(e);
			em.getTransaction().commit();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean updateAuctions(Auctions e) {
		try {
			em.getTransaction().begin();
			em.merge(e);
			em.getTransaction().commit();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean removeAuctions(String id) {
		try {
			Auctions equipo = em.find(Auctions.class, id);
			em.getTransaction().begin();
			em.remove(equipo);
			em.getTransaction().commit();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<Auctions> getAuctions() {
		List<Auctions> auctions = null;
		TypedQuery<Auctions> q = em.createNamedQuery("Auctions.findAll", Auctions.class);
		try {
			auctions = q.getResultList();
		} catch (NoResultException e) {
			auctions = new ArrayList<Auctions>();
		}

		return auctions;
	}

	public Auctions getAuctionsById(String id) {
		Auctions auction = em.find(Auctions.class, id);
		return auction;
	}

	public List<Auctions> getAuctionsByType(String type) {

		List<Auctions> auctions = null;
		TypedQuery<Auctions> query = em.createNamedQuery("Auctions.findByType", Auctions.class);

		try {
			query.setParameter("type", type);
			auctions = query.getResultList();
		} catch (NoResultException e) {
			auctions = new ArrayList<Auctions>();
		}

		return auctions;
	}

	public List<Auctions> getAuctionsByTime(String time) {

		List<Auctions> auctions = null;
		TypedQuery<Auctions> query = em.createNamedQuery("Auctions.findByTime", Auctions.class);

		try {
			query.setParameter("time", time);
			auctions = query.getResultList();
		} catch (NoResultException e) {
			auctions = new ArrayList<Auctions>();
		}

		return auctions;
	}

	public List<Auctions> getAuctionsByPrice(String price) {

		List<Auctions> auctions = null;
		TypedQuery<Auctions> query = em.createNamedQuery("Auctions.findByPrice", Auctions.class);

		try {
			query.setParameter("price", price);
			auctions = query.getResultList();
		} catch (NoResultException e) {
			auctions = new ArrayList<Auctions>();
		}

		return auctions;
	}

}
