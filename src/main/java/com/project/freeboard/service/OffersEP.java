package com.project.freeboard.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.jdo.annotations.Transactional;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.NotFoundException;
import com.project.freeboard.dao.AuctionsDAO;
import com.project.freeboard.dao.OffersDAO;
import com.project.freeboard.entity.Auctions;
import com.project.freeboard.entity.Offers;
import com.project.freeboard.entity.Students;

@Api(name = "freeboard", version = "v1", namespace = @ApiNamespace(ownerDomain = "service.freeboard.project.com", ownerName = "service.freeboard.project.com", packagePath = ""))
public class OffersEP {

//	private OffersDAO oDAO;
//
//	private AuctionsDAO aDAO;
//
//	@ApiMethod(name = "addOffers", path = "offers/{auctionsid}", httpMethod = ApiMethod.HttpMethod.POST)
//	public Offers addOffers(@Named("price") String price, @Named("students_cc") String students_cc,
//			@Named("auctions_idauctions") int auctions_idauctions, @Named("auctionsid") String auctionsid)
//			throws NotFoundException {
//
//		oDAO = new OffersDAO();
//		String idoffers = createHash();
//		Date created = getCurrentDate();
//		String state = "pending";
//
//		Auctions actualAuction = aDAO.getAuctionsById(auctionsid);
//		Offers offer = new Offers(idoffers, state, created);
//		offer.setAuctionsIdauctions(actualAuction);
//		aDAO.getAuctionsById(auctionsid).getOffersList().add(offer);
//		if (oDAO.addOffers(offer)) {
//			return offer;
//		} else {
//			throw new NotFoundException("Offers no se pudo agregar.");
//		}
//	}
//
//	@ApiMethod(name = "updateOffers", path = "offers", httpMethod = ApiMethod.HttpMethod.PUT)
//	public Offers updateOffers(Offers e) throws NotFoundException {
//		oDAO = new OffersDAO();
//		if (oDAO.updateOffers(e)) {
//			return e;
//		} else {
//			throw new NotFoundException("Offer no existe.");
//		}
//	}
//
//	@Transactional
//	@ApiMethod(name = "removeOffers", path = "offers/{id}", httpMethod = ApiMethod.HttpMethod.DELETE)
//	public void removeOffers(@Named("id") String id) throws NotFoundException {
//		oDAO = new OffersDAO();
//		if (!oDAO.removeOffers(id)) {
//			throw new NotFoundException("Offer no existe.");
//		}
//	}
//
//	@ApiMethod(name = "getAllOffers", path = "offers", httpMethod = ApiMethod.HttpMethod.GET)
//	public List<Offers> getOffers() {
//
//		oDAO = new OffersDAO();
//		List<Offers> Offers = oDAO.getOffers();
//
//		return Offers;
//	}
//
//	@ApiMethod(name = "getOffersById", path = "offers/{id}", httpMethod = ApiMethod.HttpMethod.GET)
//	public Offers getOffersById(@Named("id") String id) throws NotFoundException {
//		oDAO = new OffersDAO();
//		Offers Offers = oDAO.getOffersById(id);
//		if (Offers != null) {
//			return Offers;
//		} else {
//			throw new NotFoundException("Offer no existe.");
//		}
//	}
//
//	@ApiMethod(name = "getStudentId", path = "offers/studentid/{studentid}", httpMethod = ApiMethod.HttpMethod.GET)
//	public Students getStudentId(@Named("studentid") String id) throws NotFoundException {
//		oDAO = new OffersDAO();
//		Offers offer = getOffersById(id);
//		Students student = offer.getStudentsId();
//
//		if (offer != null || student != null) {
//			return student;
//		} else {
//			throw new NotFoundException("Oferta o freboard no existen.");
//		}
//	}
//
//	@ApiMethod(name = "getAuctionId", path = "offers/auctionid/{auctionid}", httpMethod = ApiMethod.HttpMethod.GET)
//	public Auctions getAuctionsIdauctions(@Named("studentid") String id) throws NotFoundException {
//		oDAO = new OffersDAO();
//		Offers offer = getOffersById(id);
//		Auctions auction = offer.getAuctionsIdauctions();
//
//		if (offer != null || auction != null) {
//			return auction;
//		} else {
//			throw new NotFoundException("Oferta o subasta no existen.");
//		}
//	}
//
//	private Date getCurrentDate() {
//		return Calendar.getInstance().getTime();
//	}
//
//	private String createHash() {
//		return UUID.randomUUID().toString().replaceAll("-", "");
//	}
}
