package com.project.freeboard.util;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.crypto.spec.SecretKeySpec;
import com.project.freeboard.entity.Companies;
import com.project.freeboard.entity.Students;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWT {

	public static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

	public static final byte[] SECRET = "agskgHh2971pA_SaLSDOAaÑSaS[A*sdañSDs:aSX:as!!!;asMAS?MXA:SD=AÑS)JLQ)LS)AJS)AS=Gq=="
			.getBytes();
	public static final Key SIGNING_KEY = new SecretKeySpec(SECRET, SIGNATURE_ALGORITHM.getJcaName());

	private String jwt;

	public JWT(String value) {
		this.jwt = value;
	}

	public String getValue() {
		return jwt;
	}

	public void setValue(String value) {
		this.jwt = value;
	}

	/**
	 * * Generate the access token to a company and its expiration date in order
	 * to be used within * 10 minutes based on the system's current date time *
	 * * @param user * The user who will be configured with his access
	 * properties
	 */
	public static String generateToken(Companies companies) {
		JwtBuilder builder = Jwts.builder().setId(companies.getEmail())
				.setIssuedAt(GregorianCalendar.getInstance().getTime()).setExpiration(generateTokenExpiration())
				.signWith(SIGNATURE_ALGORITHM, SIGNING_KEY);
		return builder.compact();
	}

	/**
	 * * Generate the access token to a student and its expiration date in order
	 * to be used within * 10 minutes based on the system's current date time *
	 * * @param user * The user who will be configured with his access
	 * properties
	 */
	public static String generateToken(Students student) {
		JwtBuilder builder = Jwts.builder().setId(student.getEmail())
				.setIssuedAt(GregorianCalendar.getInstance().getTime()).setExpiration(generateTokenExpiration())
				.signWith(SIGNATURE_ALGORITHM, SIGNING_KEY);
		return builder.compact();
	}

	/**
	 * * Generate an expiration date adding 10 minutes to the system's current
	 * date * time * * @return The expiration date
	 */
	private static Date generateTokenExpiration() {
		GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
		calendar.add(Calendar.HOUR, 24);
		return calendar.getTime();
	}

}
