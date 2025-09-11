package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/CheckRegistration")
public class CheckRegistration extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public CheckRegistration() {
		super();
	}
	
	public void init() throws ServletException {
		this.connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doPost(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String passwordCheck = request.getParameter("passwordCheck");

		
		
		//check if the parameters are not empty or null
		if(username == null || password == null || name == null || surname == null || passwordCheck == null || username.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty() || passwordCheck.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		//Check if the parameters are too long
		if(username.length() > 50 || password.length() > 50 || name.length() > 50 || surname.length() > 50 ){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Input too long (MAX LENGTH is 50);");
			return;
		}
	
		//Check if the Password and CheckPassword are identical
		if(!password.equals(passwordCheck)){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"THE PASSWORDS ENTERED ARE DIFFERENT;");
			return;
		}
		
		if (username.contains(" ") || password.contains(" ") || name.contains(" ") || surname.contains(" ")) {
		    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameters cannot contain spaces");
		    return;
		}
					
		UserDAO userDao = new UserDAO(connection);
		
		try {
			userDao.createUser(username, password, name, surname);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't create Account because Username is taken");
			return;
		}	
		
		String path = getServletContext().getContextPath() + "/index.html";
		response.sendRedirect(path);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}