package com.project.freeboard.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.project.freeboard.dao.CompaniesDAO;
import com.project.freeboard.entity.Companies;

public class loginCompany extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(loginCompany.class.getName());

	private CompaniesDAO cDAO;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String email = request.getParameter("email"); // idTransaccion
		String password = request.getParameter("password");

		if (email != null && !email.equals("") && password != null && !password.equals("")) {

			cDAO = new CompaniesDAO();
			Companies companies = cDAO.getCompanyByEmail(email);
			if (companies == null) {
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.sendError(400, "Email doesn't exists.");

			} else if (companies.getPassword().equals(password)) {
				String uid = createHash();
				try {

					// InputStream is = getServletContext()
					// .getResourceAsStream("/WEB-INF/freeboard-develop-firebase-adminsdk-ukcay-21b537b3bc.json");
					// FirebaseOptions options = new FirebaseOptions.Builder()
					// .setCredential(FirebaseCredentials.fromCertificate(is))
					// .setDatabaseUrl("https://freeboard-develop.firebaseio.com").build();
					//
					// FirebaseApp.initializeApp(options);
					//
					// Task<String> authTask =
					// FirebaseAuth.getInstance().createCustomToken(uid);
					//
					// try {
					// Tasks.await(authTask);
					// } catch (ExecutionException | InterruptedException e) {
					// logger.log(Level.SEVERE, e.toString(), e);
					// }
					// response.setContentType("text/plain");
					// response.setCharacterEncoding("UTF-8");
					// String jwtToken = authTask.getResult();
					// response.getWriter().println("EL JWT TOKEN ES:" +
					// jwtToken);

				} catch (Exception e) {
					e.printStackTrace();
					response.setContentType("text/html");
					response.sendError(500, e.getCause().getMessage());
				}

			} else {
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.sendError(400, "Password incorrect.");
			}

		} else

		{
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			response.sendError(400, "Fill all the required fields.");
		}

	}

	private String createHash() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
