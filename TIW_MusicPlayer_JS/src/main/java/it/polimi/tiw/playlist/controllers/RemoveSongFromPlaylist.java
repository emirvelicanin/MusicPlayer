package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/RemoveSongFromPlaylist")
@MultipartConfig
public class RemoveSongFromPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

    public RemoveSongFromPlaylist() {
        super();
    }

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		
		String songIDString = request.getParameter("songId");
		String playlistIDString = request.getParameter("playlistId");
		
		if(songIDString == null || songIDString.isEmpty() || playlistIDString == null || playlistIDString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters");
			return;
		}
		
		int songId;
		int playlistId;
		try {
			songId = Integer.parseInt(songIDString);
			playlistId = Integer.parseInt(playlistIDString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid parameters");
			return;
		}
		
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		try {
			pDao.removeSongFromPlaylist(songId, playlistId, user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Failure in removing song");
			return;
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