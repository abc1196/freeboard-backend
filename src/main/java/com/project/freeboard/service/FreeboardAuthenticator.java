package com.project.freeboard.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

public class FreeboardAuthenticator implements Authenticator {

	private static final Logger logger = Logger.getLogger(FreeboardAuthenticator.class.getName());

	static {
		try {
			InputStream is = new FileInputStream(new File(
					"./freeboard/src/main/webapp/WEB-INF/freeboard-develop-firebase-adminsdk-ukcay-21b537b3bc.json"));
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredential(FirebaseCredentials.fromCertificate(is))
					.setDatabaseUrl("https://freeboard-develop.firebaseio.com").build();

			FirebaseApp.initializeApp(options);

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	@Override
	public User authenticate(HttpServletRequest httpServletRequest) {

		// get token
		final String authorizationHeader = httpServletRequest.getHeader("Authorization");

		// verify
		if (authorizationHeader != null) {
			Task<FirebaseToken> task = FirebaseAuth.getInstance()
					.verifyIdToken(authorizationHeader.replace("Bearer ", ""));

			// wait for the task
			try {
				Tasks.await(task);
			} catch (ExecutionException e) {
			} catch (InterruptedException e) {
			}

			FirebaseToken firebaseToken = task.getResult();
			User user = new User(firebaseToken.getUid(), firebaseToken.getEmail());
			return user;
		}

		return null;
	}

}