package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckLogin() {
		super();
	}
	
	public void init() throws ServletException {		
		this.connection = ConnectionHandler.getConnection(getServletContext());	
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response) throws ServletException,IOException{
		String userName = request.getParameter("username");
		String password = request.getParameter("password");
		
		//Check if the parameters are not empty or null
		if(userName == null || password == null || userName.isEmpty() || password.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
	
		// query database to authenticate for user
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		try {
			 user = userDAO.checkAuthentication(userName, password );
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking");
				e.printStackTrace();
				return;
				}
		
		// If the user exists, add info to the session and go to home page, otherwise return an error status		
		if (user == null) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Wrong username or password");
			return;
			} else {
				request.getSession().setAttribute("user", user);
				String path = getServletContext().getContextPath() + "/GoToHomePage";
				response.sendRedirect(path);
			}			
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}