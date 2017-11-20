package com.project.freeboard.service;

import java.util.ArrayList;
import com.project.freeboard.util.Message;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.jdo.annotations.Transactional;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.project.freeboard.dao.AuctionsDAO;
import com.project.freeboard.dao.CompaniesDAO;
import com.project.freeboard.dao.OffersDAO;
import com.project.freeboard.entity.Auctions;
import com.project.freeboard.entity.Companies;
import com.project.freeboard.entity.Offers;
import com.project.freeboard.entity.Students;
import com.project.freeboard.util.JWT;
import com.project.freeboard.util.PersistenceManager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

/**
 * @author alejo
 *
 */
@Api(name = "companies", version = "v1", namespace = @ApiNamespace(ownerDomain = "service.freeboard.project.com", ownerName = "service.freeboard.project.com", packagePath = "/companies"))
public class CompaniesEP {

	private final static String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

	private final static String PHONE_PATTERN = "^\\+(?:[0-9] ?){6,14}[0-9]$";

	private final static int PASSWORD_MIN_LENGTH = 8;

	private EntityManager em = PersistenceManager.getEntityManager();

	private CompaniesDAO cDAO = new CompaniesDAO(em);

	private AuctionsDAO aDAO = new AuctionsDAO(em);

	private OffersDAO oDAO = new OffersDAO(em);

	/**
	 * API Company Entity
	 * 
	 * 
	 */

	/**
	 * signUpCompany Allows to signup and a new company to freeboard.
	 * 
	 * @param email
	 * @param name
	 * @param phone
	 * @param address
	 * @param password
	 * @param contactPerson
	 * @return Returns the new company
	 * @throws BadRequestException
	 *             * When the client sends invalid parameters.
	 * @throws NotFoundException
	 *             * When the company couldn't be added.
	 */
	@ApiMethod(name = "signUpCompany", path = "signup/company", httpMethod = ApiMethod.HttpMethod.POST)
	public Companies signUpCompany(Companies companies) throws BadRequestException, NotFoundException {
		if (companies!=null) {

			if (cDAO.getCompanyByEmail(companies.getEmail()) != null) {
				throw new BadRequestException("Email already in use.");
			}
			ArrayList<String> invalidInputs = new ArrayList<String>();
			if (!isValidEmail(companies.getEmail())) {
				invalidInputs.add("Email");
			}
			if (!isValidPhone(companies.getPhone())) {
				invalidInputs.add("Phone");
			}
			if (!isValidPassword(companies.getPassword())) {
				invalidInputs.add("Password");
			}
			if (!invalidInputs.isEmpty()) {
				String invalidInputsLog = "";
				for (int i = 0; i < invalidInputs.size(); i++) {
					invalidInputsLog += invalidInputs.get(i) + "-";
				}
				throw new BadRequestException(invalidInputsLog);
			}
			String hash = createHash();
			Date created = getCurrentDate();
			companies.setHash(hash);
			companies.setCreated(created);
			if (cDAO.addCompany(companies)) {

				return companies;
			} else {
				throw new NotFoundException("Company not added.");
			}
		} else {
			throw new BadRequestException("Fill all the required fields.");
		}
	}

	/**
	 * updateCompany Allows to update the values of the given company.
	 * 
	 * @param c
	 * @return Returns the updated company.
	 * @throws NotFoundException
	 *             * If the given company doesn't exists.
	 */
	@ApiMethod(name = "updateCompany", path = "update/company", httpMethod = ApiMethod.HttpMethod.PUT)
	public Companies updateCompany(Companies c) throws NotFoundException {

		c.setUpdated(getCurrentDate());
		if (cDAO.updateCompanie(c)) {
			return c;
		} else {
			throw new NotFoundException("Company doesn't exist.");
		}
	}

	/**
	 * loginCompany Allows to sign in the given student validating the email and
	 * password.
	 * 
	 * @param email
	 * @param password
	 * @return Returns the JWT generated for the user.
	 * @throws NotFoundException
	 *             * If the company doesn't exists.
	 * @throws BadRequestException
	 *             * When the client sends invalid parameters.
	 */
	@ApiMethod(name = "loginCompany", path = "login/company", httpMethod = ApiMethod.HttpMethod.POST)
	public JWT loginCompany(@Named("email") String email, @Named("password") String password)
			throws NotFoundException, BadRequestException {
		if (email != null && !email.equals("") && password != null && !password.equals("")) {

			// cDAO = new CompaniesDAO();
			Companies companies = cDAO.getCompanyByEmail(email);
			if (companies == null) {
				throw new NotFoundException("Email doesn't Exist.");
			} else if (companies.getPassword().equals(password)) {
				return new JWT(JWT.generateToken(companies));
			} else {
				throw new BadRequestException("Password Incorrect.");
			}

		} else {
			throw new BadRequestException("Fill all the required fields.");
		}

	}

	/**
	 * removeCompany Allows the removen a company by its email.
	 * 
	 * @param email
	 * @return Returns the removed company.
	 * @throws NotFoundException
	 *             * If the company doesn't exists.
	 * @throws BadRequestException
	 */
	@Transactional
	@ApiMethod(name = "removeCompany", path = "remove/company/{email}", httpMethod = ApiMethod.HttpMethod.DELETE)
	public Companies removeCompany(@Named("email") String email) throws NotFoundException, BadRequestException {
		if (email != null) {
			Companies companies = cDAO.removeCompanie(email);
			if (companies != null) {
				return companies;
			} else {
				throw new NotFoundException("Company doesn't exist.");
			}
		} else {
			throw new BadRequestException("invalid parameters");
		}

	}

	/**
	 * getCompanies
	 * 
	 * @return Returns all the companies in freeboard.
	 */
	@ApiMethod(name = "getAllCompanies", path = "companies", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Companies> getCompanies() {

		List<Companies> companies = cDAO.getCompanies();

		return companies;
	}

	/**
	 * getCompanyByEmail Allows to return a company by its email
	 * 
	 * @param email
	 * @return The required company
	 * @throws NotFoundException
	 *             * If the company doesn't exists
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "getCompanyByEmail", path = "companies/email/{email}", httpMethod = ApiMethod.HttpMethod.GET)
	public Companies getCompanyByEmail(@Named("email") String email) throws NotFoundException, BadRequestException {
		if (email != null) {
			Companies companies = cDAO.getCompanyByEmail(email);
			if (companies != null) {
				return companies;
			} else {
				throw new NotFoundException("Company doesn't exist.");
			}
		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * getCompanyByName Allows to return a company by its name
	 * 
	 * @param name
	 * @return The required company
	 * @throws NotFoundException
	 *             * If the company doesn't exists
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "getCompanyByName", path = "companies/name/{name}", httpMethod = ApiMethod.HttpMethod.GET)
	public Companies getCompanyByName(@Named("name") String name) throws NotFoundException, BadRequestException {
		if (name != null) {
			Companies companies = cDAO.getCompanieByName(name);
			if (companies != null) {
				return companies;
			} else {
				throw new NotFoundException("Company doesn't exist.");
			}
		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * addAuction Allows to add a new auction associated to a company.
	 * 
	 * @param jwt
	 * @param name
	 * @param type
	 * @param size
	 * @param mainColor
	 * @param secundaryColor
	 * @param description
	 * @param time
	 * @param price
	 * @return Returns the new auction
	 * @throws BadRequestException
	 *             * When the client sends invalid parameters.
	 * @throws NotFoundException
	 *             * When the auction couldn't be added.
	 * @throws UnauthorizedException
	 *             * If the token isn't valid. If the token is expired
	 */
	@ApiMethod(name = "addAuction", path = "companies/addauction", httpMethod = ApiMethod.HttpMethod.POST)
	public Auctions addAuction(@Named("jwt") String jwt, @Named("name") String name, @Named("type") String type,
			@Named("size") String size, @Named("mainColor") String mainColor,
			@Named("secundaryColor") String secundaryColor, @Named("description") String description,
			@Named("date") Date time, @Named("price") String price)
			throws NotFoundException, UnauthorizedException, BadRequestException {

		if (jwt != null && name != null && type != null && secundaryColor != null && description != null && time != null
				&& time != null && price != null) {
			try {
				Double.parseDouble(price);
			} catch (NumberFormatException ne) {
				throw new BadRequestException("invalid price");
			}
			Companies companie = getCurrentCompany(jwt);
			String id = createHash();
			Date created = getCurrentDate();
			Auctions auction = new Auctions(id, name, type, size, mainColor, secundaryColor, description, time, price,
					created);
			if (cDAO.addAuctions(auction, companie)) {
				return auction;
			} else {
				throw new NotFoundException("Auction not added.");
			}
		} else {
			throw new BadRequestException("invalid parameters");
		}

	}

	/**
	 * updateAuction Allows to update the values of the given auction.
	 * 
	 * @param jwt
	 * @param a
	 * @return Returns the updated auction.
	 * @throws UnauthorizedException
	 *             * If the token isn't valid. If the token is expired
	 * @throws NotFoundException
	 *             * If the given auction doesn't exists.
	 * @throws BadRequestException
	 *             * When the client sends invalid parameters.
	 */
	@ApiMethod(name = "updateAuction", path = "updateAuction", httpMethod = ApiMethod.HttpMethod.PUT)
	public Auctions updateAuction(@Named("jwt") String jwt, Auctions a) throws NotFoundException, BadRequestException {
		if (jwt != null) {
			if (aDAO.updateAuctions(a)) {
				return a;
			} else {
				throw new NotFoundException("Auction doesn't exist.");
			}
		} else {
			throw new BadRequestException("empty jwt");
		}
	}

	/**
	 * removeAuction Allows the remove an auction by its id.
	 * 
	 * @param jwt
	 * @param email
	 * @return Returns the removed auctions.
	 * @throws NotFoundException
	 *             * If the auction doesn't exists.
	 * @throws BadRequestException
	 *             * When the client sends invalid parameters
	 */
	@Transactional
	@ApiMethod(name = "removeAuction", path = "removeAuction/{id}", httpMethod = ApiMethod.HttpMethod.DELETE)
	public Message removeAuction(@Named("jwt") String jwt, @Named("id") String id)
			throws NotFoundException, UnauthorizedException, BadRequestException {

		if (jwt != null && id != null) {
			getCurrentCompany(jwt);
			if (aDAO.removeAuctions(id)) {
				return new Message("Subasta eliminada");
			} else {
				throw new NotFoundException("Auction doesn't exist.");
			}

		} else {
			throw new BadRequestException("Invalid parameters");
		}
	}

	/**
	 * getAuctions
	 * 
	 * @return Returns all the auctions in freeboard.
	 */
	@ApiMethod(name = "getAllAuctions", path = "auctions", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Auctions> getAuctions() {

		List<Auctions> auctions = aDAO.getAuctions();

		return auctions;
	}

	/**
	 * getStudentIdstudents Allows to retrieve the student associated to an
	 * offer in an auction.
	 * 
	 * @param jwt
	 * @param id
	 * @return
	 * @throws NotFoundException
	 *             * If the auction doesn't exists.
	 * @throws UnauthorizedException
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "getStudentId", path = "offers/auction/{auctionid}", httpMethod = ApiMethod.HttpMethod.GET)
	public Students getStudentIdstudents(@Named("jwt") String jwt, @Named("auctionid") String id)
			throws NotFoundException, UnauthorizedException, BadRequestException {

		if (jwt != null && id != null) {
			getCurrentCompany(jwt);
			Auctions auction = aDAO.getAuctionsById(id);
			if (auction != null) {
				Offers offer = auction.getWinnerOffer();
				Students student = offer.getStudentsId();
				return student;
			} else {
				throw new NotFoundException("auction does not exists");
			}
		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	@ApiMethod(name = "getAllAuctionsByCompany", path = "offers", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Auctions> getAllAuctionsByCompany(@Named("jwt") String jwt)
			throws UnauthorizedException, BadRequestException {

		if (jwt != null) {

			Companies companie = getCurrentCompany(jwt);
			List<Auctions> myAuctions = aDAO.getAuctionsByCompany(companie);
			return myAuctions;
		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * getAuctionById Allows to return an auction by its id
	 * 
	 * @param id
	 * @return The required auction
	 * @throws NotFoundException
	 *             * If the auction doesn't exists
	 */
	@ApiMethod(name = "getAuctionByIdc", path = "auctionsByIdc/{id}", httpMethod = ApiMethod.HttpMethod.GET)
	public Auctions getAuctionByIdc(@Named("id") String id) throws NotFoundException {

		Auctions auction = aDAO.getAuctionsById(id);
		if (auction != null) {
			return auction;
		} else {
			throw new NotFoundException("Auction doesn't exist.");
		}
	}

	/**
	 * getAuctionById Allows to return an auction by its id
	 * 
	 * @param jwt
	 * @param id
	 * @return The required auction
	 * @throws NotFoundException
	 *             * If the auction doesn't exists
	 * @throws UnauthorizedException
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "getAuctionById", path = "auctionsById/{id}", httpMethod = ApiMethod.HttpMethod.GET)
	public Auctions getAuctionById(@Named("jwt") String jwt, @Named("id") String id)
			throws NotFoundException, UnauthorizedException, BadRequestException {
		if (jwt != null && id != null) {
			getCurrentCompany(jwt);
			Auctions auction = aDAO.getAuctionsById(id);
			if (auction != null) {
				return auction;
			} else {
				throw new NotFoundException("Auction doesn't exist.");
			}
		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * getAuctionByType Allows to return an auction by its type
	 * 
	 * @param jwt
	 * @param type
	 * @return The required auction
	 * @throws NotFoundException
	 *             * If the auction doesn't exists
	 * @throws UnauthorizedException
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "getAuctionByType", path = "auctionsByType/{type}", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Auctions> getAuctionByType(@Named("jwt") String jwt, @Named("type") String type)
			throws NotFoundException, UnauthorizedException, BadRequestException {
		if (jwt != null && type != null) {
			getCurrentCompany(jwt);
			List<Auctions> auction = aDAO.getAuctionsByType(type);
			return auction;
		} else {
			throw new BadRequestException("invalid parameters");
		}

	}

	/**
	 * getAuctionByTime Allows to return an auction by its time
	 * 
	 * @param jwt
	 * @param time
	 * @return The required auction
	 * @throws NotFoundException
	 *             * If the auction doesn't exists
	 * @throws UnauthorizedException
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "getAuctionByTime", path = "auctionsByTime/{time}", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Auctions> getAuctionByTime(@Named("jwt") String jwt, @Named("id") String time)
			throws NotFoundException, UnauthorizedException, BadRequestException {
		if (jwt != null && time != null) {
			getCurrentCompany(jwt);
			List<Auctions> auction = aDAO.getAuctionsByTime(time);
			return auction;

		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * getAuctionByPrice Allows to return an auction by its price
	 * 
	 * @param jwt
	 * @param price
	 * @return The required auction
	 * @throws NotFoundException
	 *             * If the auction doesn't exists
	 * @throws UnauthorizedException
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "getAuctionByPrice", path = "auctionsByPrice/{price}", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Auctions> getAuctionByPrice(@Named("jwt") String jwt, @Named("price") String price)
			throws NotFoundException, UnauthorizedException, BadRequestException {
		if (jwt != null && price != null) {
			try {
				Double.parseDouble(price);
			} catch (NumberFormatException ne) {
				throw new BadRequestException("invalid price");
			}
			getCurrentCompany(jwt);
			List<Auctions> auction = aDAO.getAuctionsByPrice(price);
			return auction;

		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * selectWinnerOffer Allows the select an auction's winner offer.
	 * 
	 * @param jwt
	 * @param offerid
	 * @param auctionid
	 * @return Returns the winner offer
	 * @throws UnauthorizedException
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "selectWinnerOffer", path = "auctionsWinnerOffer/", httpMethod = ApiMethod.HttpMethod.POST)
	public Offers selectWinnerOffer(@Named("jwt") String jwt, @Named("offerid") String offerid,
			@Named("auctionid") String auctionid) throws UnauthorizedException, BadRequestException {
		if (jwt != null & offerid != null && auctionid != null) {
			getCurrentCompany(jwt);
			Offers winner = oDAO.getOffersById(offerid);
			winner.setState(Offers.ACCEPTED);
			aDAO.getAuctionsById(auctionid).setWinnerOffer(winner);
			oDAO.updateOffers(winner);
			return winner;
		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * showOffers Allows to show the offers of an auction created by a company.
	 * 
	 * @param jwt
	 * @param auctionid
	 * @return Returns the list of offers.
	 * @throws UnauthorizedException
	 * @throws BadRequestException
	 */
	@ApiMethod(name = "showOffers", path = "auctionsShowOffers/{auctionid}", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Offers> showOffers(@Named("jwt") String jwt, @Named("auctionid") String auctionid)
			throws UnauthorizedException, BadRequestException {
		if (jwt != null && auctionid != null) {
			getCurrentCompany(jwt);
			List<Offers> offersList = aDAO.getAuctionsById(auctionid).getOffersList();
			return offersList;
		} else {
			throw new BadRequestException("invalid parameters");
		}

	}

	/**
	 * closeAuction Allows to deny the non winning offers.
	 * 
	 * @param jwt
	 * @param auctionid
	 * @return Returns the winner offer
	 * @throws BadRequestException
	 * @throws UnauthorizedException
	 */
	@ApiMethod(name = "closeAuction", path = "auctionsClose/{auctionid}", httpMethod = ApiMethod.HttpMethod.GET)
	public Offers closeAuction(@Named("jwt") String jwt, @Named("auctionid") String auctionid)
			throws BadRequestException, UnauthorizedException {
		if (jwt != null && auctionid != null) {
			getCurrentCompany(jwt);
			Auctions auction = aDAO.getAuctionsById(auctionid);
			Offers winner = null;
			for (int i = 0; i < auction.getOffersList().size(); i++) {

				if (auction.getOffersList().get(i).getState().equals(Offers.ACCEPTED)) {
					winner = auction.getOffersList().get(i);
				} else {
					auction.getOffersList().get(i).setState(Offers.DENIED);
				}
			}

			aDAO.updateAuctions(auction);
			return winner;
		} else {
			throw new BadRequestException("invalid parameters");
		}
	}

	/**
	 * * Allows retrieve the name, lastname and email of the logged user *
	 * <p>
	 * * * @param token * the JSON Web Token * @return The logged user
	 * information * @throws UnauthorizedException * If the token isn't valid If
	 * the token is expired
	 */
	private Companies getCurrentCompany(String token) throws UnauthorizedException {
		String userEmail = null;
		Companies user = null;
		try {
			Claims claims = Jwts.parser().setSigningKey(JWT.SECRET).parseClaimsJws(token).getBody();
			userEmail = claims.getId();
		} catch (SignatureException e) {
			throw new UnauthorizedException("Invalid token");
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException("Expired token");
		}

		if (userEmail != null) {

			user = cDAO.getCompanyByEmail(userEmail);

			if (user == null) {
				throw new UnauthorizedException("Invalid access");
			}
		}
		return user;
	}

	/**
	 * getCurrentDate
	 * 
	 * @return current date
	 */
	private Date getCurrentDate() {
		return Calendar.getInstance().getTime();
	}

	/**
	 * createHash
	 * 
	 * @return uuid hash
	 */
	private String createHash() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * isValidEmail
	 * 
	 * @param email
	 * @return if email is valid or not by EMAIL_PATTERN
	 */
	private boolean isValidEmail(String email) {
		Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
		try {
			InternetAddress address = new InternetAddress(email);
			address.validate();
			return emailPattern.matcher(email).matches();
		} catch (AddressException e) {
			return false;
		}

	}

	/**
	 * isValidPhone
	 * 
	 * @param phone
	 * @return if phone is valid or not by PHONE_PATTERN
	 */
	private boolean isValidPhone(String phone) {
		Pattern phonePattern = Pattern.compile(PHONE_PATTERN);
		return phonePattern.matcher(phone).matches();
	}

	/**
	 * isValidPassword
	 * 
	 * @param password
	 * @return if password.length >= PASSWORD_MIN_LENGTH
	 */
	private boolean isValidPassword(String password) {
		return password.length() >= PASSWORD_MIN_LENGTH;
	}

}
