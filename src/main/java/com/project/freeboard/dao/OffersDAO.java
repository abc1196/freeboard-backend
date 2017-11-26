package com.project.freeboard.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.project.freeboard.entity.Auctions;
import com.project.freeboard.entity.Offers;
import com.project.freeboard.entity.Students;

public class OffersDAO {

	private EntityManager em;

	public OffersDAO(EntityManager em) {
		this.em = em;
	}

	public boolean addOffers(Offers o) {
		// Check for already exists
		try {
			em.getTransaction().begin();
			em.persist(o);
			em.getTransaction().commit();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean updateOffers(Offers o) {
		try {
			em.getTransaction().begin();
			em.merge(o);
			em.getTransaction().commit();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public boolean removeOffers(String id) {
		try {
			Offers Offers = em.find(Offers.class, id);
			em.getTransaction().begin();
			em.remove(Offers);
			em.getTransaction().commit();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<Offers> getOffers() {
		List<Offers> Offerss = null;
		TypedQuery<Offers> q = em.createNamedQuery("Offers.findAll", Offers.class);
		try {
			Offerss = q.getResultList();
		} catch (NoResultException e) {
			Offerss = new ArrayList<Offers>();
		}
		return Offerss;
	}

	public List<Offers> getOfferssByStudent(Students student) {
		List<Offers> Offerss = null;
		TypedQuery<Offers> q = em.createNamedQuery("Offers.getOffersByStudent", Offers.class);
		q.setParameter("studentsId", student);
		try {
			Offerss = q.getResultList();
		} catch (NoResultException e) {
			Offerss = new ArrayList<Offers>();
		}
		return Offerss;
	}

	public List<Offers> getOfferssByAuction(Auctions id) {
		List<Offers> Offerss = null;
		TypedQuery<Offers> q = em.createNamedQuery("Offers.getOffersByAuction", Offers.class);
		q.setParameter("auctionsIdauctions", id);
		try {
			Offerss = q.getResultList();
		} catch (NoResultException e) {
			Offerss = new ArrayList<Offers>();
		}
		return Offerss;
	}

	public Offers getOffersById(String id) {

		Offers Offers = em.find(Offers.class, id);
		return Offers;
	}

}
