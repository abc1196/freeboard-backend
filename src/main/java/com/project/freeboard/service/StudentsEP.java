package com.project.freeboard.service;

import java.security.Key;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.spec.SecretKeySpec;
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
import com.project.freeboard.dao.StudentsDAO;
import com.project.freeboard.entity.Companies;
import com.project.freeboard.entity.Students;
import com.project.freeboard.util.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

@Api(name = "students", version = "v1", namespace = @ApiNamespace(ownerDomain = "service.freeboard.project.com", ownerName = "service.freeboard.project.com", packagePath = "/students"))
public class StudentsEP {

	private final static String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

	private final static String PHONE_PATTERN = "^\\+(?:[0-9] ?){6,14}[0-9]$";

	private final static int PASSWORD_MIN_LENGTH = 8;

	/**
	 * API Student Entity
	 */

	private StudentsDAO sDAO;

	@ApiMethod(name = "signUpStudent", path = "signup/student", httpMethod = ApiMethod.HttpMethod.POST)
	public Students addStudent(@Named("email") String email, @Named("name") String name,
			@Named("lastname") String lastname, @Named("phone") String phone, @Named("bankWire") String bankWire,
			@Named("bank") String bank, @Named("accountType") String accountType,
			@Named("university") String university, @Named("career") String career,
			@Named("accountOwner") String accountOwner, @Named("password") String password)
			throws NotFoundException, BadRequestException {

		if (email != null && !email.equals("") && name != null && !name.equals("") && phone != null && !phone.equals("")
				&& lastname != null && !lastname.equals("") && name != null && !name.equals("")) {
			sDAO = new StudentsDAO();
			if (sDAO.getStudentByEmail(email) != null) {
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
			Students s = new Students(email, name, lastname, password, university, phone, bankWire, bank, accountType,
					accountOwner, hash, created);
			if (sDAO.addStudent(s)) {
				return s;
			} else {
				throw new NotFoundException("Student not added.");
			}
		} else {
			throw new BadRequestException("Fill all the required fields.");
		}
	}

	@ApiMethod(name = "updateStudent", path = "update/student", httpMethod = ApiMethod.HttpMethod.PUT)
	public Students updateStudent(Students s) throws NotFoundException {
		sDAO = new StudentsDAO();
		if (sDAO.updateStudent(s)) {
			return s;
		} else {
			throw new NotFoundException("Student doesn't exist.");
		}
	}

	@Transactional
	@ApiMethod(name = "removeStudent", path = "remove/student/{email}", httpMethod = ApiMethod.HttpMethod.DELETE)
	public Students removeStudent(@Named("email") String email) throws NotFoundException {
		sDAO = new StudentsDAO();
		Students students = sDAO.removeStudent(email);
		if (students != null) {
			return students;
		} else {
			throw new NotFoundException("Student doesn't exist.");
		}
	}

	@ApiMethod(name = "getAllStudents", path = "students", httpMethod = ApiMethod.HttpMethod.GET)
	public List<Students> getStudents() {

		sDAO = new StudentsDAO();
		List<Students> students = sDAO.getStudents();

		return students;
	}

	@ApiMethod(name = "getStudentByEmail", path = "students/email/{email}", httpMethod = ApiMethod.HttpMethod.GET)
	public Students getStudentByCC(@Named("email") String email) throws NotFoundException {
		sDAO = new StudentsDAO();
		Students students = sDAO.getStudentByEmail(email);
		if (students != null) {
			return students;
		} else {
			throw new NotFoundException("Student doesn't exist.");
		}
	}

	@ApiMethod(name = "loginStudent", path = "login/student", httpMethod = ApiMethod.HttpMethod.POST)
	public JWT loginCompany(@Named("email") String email, @Named("password") String password) throws Exception {
		if (email != null && !email.equals("") && password != null && !password.equals("")) {

			Students student = sDAO.getStudentByEmail(email);
			if (student == null) {
				throw new Exception("Email doesn't Exist.");
			} else if (student.getPassword().equals(password)) {
				return new JWT(JWT.generateToken(student));
			} else {
				throw new BadRequestException("Password Incorrect.");
			}

		} else {
			throw new BadRequestException("Fill all the required fields.");
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
			return false;
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
	public Students getCurrentStudent(String token) throws UnauthorizedException {
		String userEmail = null;
		Students user = null;
		try {
			Claims claims = Jwts.parser().setSigningKey(JWT.SECRET).parseClaimsJws(token).getBody();
			userEmail = claims.getId();
		} catch (SignatureException e) {
			throw new UnauthorizedException("Invalid token");
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException("Expired token");
		}

		if (userEmail != null) {

			user = sDAO.getStudentByEmail(userEmail);

			if (user == null) {
				throw new UnauthorizedException("Invalid access");
			}
		}
		return user;
	}
}
