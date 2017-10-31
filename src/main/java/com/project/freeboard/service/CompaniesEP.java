package com.project.freeboard.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.jdo.annotations.Transactional;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.project.freeboard.dao.CompaniesDAO;
import com.project.freeboard.entity.Auctions;
import com.project.freeboard.entity.Companies;
import com.project.freeboard.util.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

@Api(name = "companies", version = "v1", namespace = @ApiNamespace(ownerDomain = "service.freeboard.project.com", ownerName = "service.freeboard.project.com", packagePath = "/companies"))
public class CompaniesEP {

	private final static String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

	private final static String PHONE_PATTERN = "^\\+(?:[0-9] ?){6,14}[0-9]$";

	private final static int PASSWORD_MIN_LENGTH = 8;

	private CompaniesDAO cDAO = new CompaniesDAO();

	/**
	 * API Company Entity
	 * 
	 * @throws Exception
	 */
	@ApiMethod(name = "signUpCompany", path = "signup/company", httpMethod = ApiMethod.HttpMethod.POST)
	public Companies signUpCompany(@Named("email") String email, @Named("name") String name,
			@Named("phone") String phone, @Named("address") String address, @Named("password") String password,
			@Named("contactPerson") String contactPerson) throws BadRequestException, NotFoundException {
		if (email != null && !email.equals("") && name != null && !name.equals("") && phone != null && !phone.equals("")
				&& address != null && !address.equals("") && password != null && !password.equals("")
				&& contactPerson != null && !contactPerson.equals("")) {

			if (cDAO.getCompanyByEmail(email) != null) {
				throw new BadRequestException("Email already in use.");
			}
			ArrayList<String> invalidInputs = new ArrayList<String>();
			if (!isValidEmail(email)) {
				invalidInputs.add("Email");
			}
			if (!isValidPhone(phone)) {
				invalidInputs.add("Phone");
			}
			if (!isValidPassword(password)) {
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
			Companies company = new Companies(email, name, phone, address, password, contactPerson, hash, created);
			if (cDAO.addCompany(company)) {

				return company;
			} else {
				throw new NotFoundException("Company not added.");
			}
		} else {
			throw new BadRequestException("Fill all the required fields.");
		}
	}

	@ApiMethod(name = "updateCompany", path = "update/company", httpMethod = ApiMethod.HttpMethod.PUT)
	public Companies updateCompany(Companies c) throws NotFoundException {
		// cDAO = new CompaniesDAO();
		Date updated = Calendar.getInstance().getTime();
		c.setUpdated(updated);
		if (cDAO.updateCompanie(c)) {
			return c;
		} else {
			throw new NotFoundException("Company doesn't exist.");
		}
	}

	@ApiMethod(name = "loginCompany", path = "login/company", httpMethod = ApiMethod.HttpMethod.POST)
	public JWT loginCompany(@Named("email") String email, @Named("password") String password) throws Exception {
		if (email != null && !email.equals("") && password != null && !password.equals("")) {

			// cDAO = new CompaniesDAO();
			Companies companies = cDAO.getCompanyByEmail(email);
			if (companies == null) {
				throw new Exception("Email doesn't Exist.");
			} else if (companies.getPassword().equals(password)) {
				return new JWT(JWT.generateToken(companies));
			} else {
				throw new BadRequestException("Password Incorrect.");
			}

		} else {
			throw new BadRequestException("Fill all the required fields.");
		}

	}

	@Transactional
	@ApiMethod(name = "removeCompany", path = "remove/company/{email}", httpMethod = ApiMethod.HttpMethod.DELETE)
	public Companies removeCompany(@Named("email") String email) throws NotFoundException {
		// cDAO = new CompaniesDAO();
		Companies companies = cDAO.removeCompanie(email);
		if (companies != null) {
			return companies;
		} else {
			throw new NotFoundException("Company doesn't exist.");
		}

	}

	@ApiMethod(name = "getAllCompanies", path = "companies", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Companies> getCompanies() {

		cDAO = new CompaniesDAO();
		List<Companies> companies = cDAO.getCompanies();

		return companies;
	}

	@ApiMethod(name = "getCompanyByEmail", path = "companies/email/{email}", httpMethod = ApiMethod.HttpMethod.GET)
	public Companies getCompanyByEmail(@Named("email") String email) throws NotFoundException {
		// cDAO = new CompaniesDAO();
		Companies companies = cDAO.getCompanyByEmail(email);
		if (companies != null) {
			return companies;
		} else {
			throw new NotFoundException("Company doesn't exist.");
		}
	}

	@ApiMethod(name = "getCompanyByName", path = "companies/name/{name}", httpMethod = ApiMethod.HttpMethod.GET)
	public Companies getCompanyByName(@Named("name") String name) throws NotFoundException {
		// cDAO = new CompaniesDAO();
		Companies companies = cDAO.getCompanieByName(name);
		if (companies != null) {
			return companies;
		} else {
			throw new NotFoundException("Company doesn't exist.");
		}
	}

	private Date getCurrentDate() {
		return Calendar.getInstance().getTime();
	}

	private String createHash() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	private boolean isValidEmail(String email) {
		Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
		try {
			InternetAddress address = new InternetAddress(email);
			address.validate();
			return emailPattern.matcher(email).matches();
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			return false;
		}

	}

	@ApiMethod(name = "addAuction", path = "companies/addauction", httpMethod = ApiMethod.HttpMethod.POST)
	public Auctions addAuction(@Named("jwt") String jwt, @Named("name") String name, @Named("type") String type,
			@Named("size") String size, @Named("mainColor") String mainColor,
			@Named("secundaryColor") String secundaryColor, @Named("description") String description,
			@Named("date") Date time, @Named("price") String price)
			throws NotFoundException, UnauthorizedException, BadRequestException {

		if (jwt != null) {

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
			throw new BadRequestException("empty jwt");
		}

	}

	private boolean isValidPhone(String phone) {
		Pattern phonePattern = Pattern.compile(PHONE_PATTERN);
		return phonePattern.matcher(phone).matches();
	}

	private boolean isValidPassword(String password) {
		return password.length() >= PASSWORD_MIN_LENGTH;
	}

	/**
	 * * Allows retrieve the name, lastname and email of the logged user *
	 * <p>
	 * * * @param token * the JSON Web Token * @return The logged user
	 * information * @throws UnauthorizedException * If the token isn't valid If
	 * the token is expired
	 */
	public Companies getCurrentCompany(String token) throws UnauthorizedException {
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

}
