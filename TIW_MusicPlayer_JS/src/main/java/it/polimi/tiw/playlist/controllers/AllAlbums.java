package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import it.polimi.tiw.playlist.beans.Album;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/AllAlbums")
public class AllAlbums extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public AllAlbums() {
        super();
    }
    
    public void init() throws ServletException {
		this.connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User user = null;
		HttpSession s = request.getSession();
		
		// Authentication check
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			user = (User) s.getAttribute("user");
		}
		
		UserDAO userDAO= new UserDAO(this.connection);
		ArrayList<Album> allAlbums = null;
		
		try {
			allAlbums = userDAO.getAllAlbums(user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Can't get all Albums");
			return;
		}
		
		// Redirect to the Home page and add allAlbums to the parameters
        
        Gson gson = new GsonBuilder()
        	.setDateFormat("dd MMM yyyy").create();
            
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(allAlbums));
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException sqle) {
		}
	}
}