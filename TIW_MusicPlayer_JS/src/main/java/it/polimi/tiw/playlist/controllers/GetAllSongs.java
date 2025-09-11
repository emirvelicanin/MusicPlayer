package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.UserDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetAllSongs")
public class GetAllSongs extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
    public GetAllSongs() {
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
		
		boolean getAlbumCovers = false;
		String getAlbumCoversString = request.getParameter("getAlbumCovers");
		if(getAlbumCoversString != null && getAlbumCoversString.equals("true")) {
			getAlbumCovers = true;
		} else {
			getAlbumCovers = false;
		}
		
		UserDAO userDao = new UserDAO(this.connection);
		ArrayList<Song> allSongs = null;
		try {
			allSongs = userDao.getAllUserSongs(user.getUsername(), getAlbumCovers);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Can't get songs from database");
			return;
		}
		
		// Redirect to the Home Page and add allSongs to the parameters
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(new Gson().toJson(allSongs));
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}