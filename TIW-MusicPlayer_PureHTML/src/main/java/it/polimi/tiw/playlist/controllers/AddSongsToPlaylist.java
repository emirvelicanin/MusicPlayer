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

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/AddSongsToPlaylist")
public class AddSongsToPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
    public AddSongsToPlaylist() {
        super();
    }

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = request.getSession();
		
		// Authentication check
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			u = (User) s.getAttribute("user");
		}
		
		String playlistIdString = request.getParameter("playlistId");
		
		// Check parameters are present
		if(playlistIdString == null || playlistIdString.isEmpty() || request.getParameterValues("songIds") == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		// Parse SongsID to number and if not possible send an error
		ArrayList<Integer> songsID = new ArrayList<>();
		try {
			for(String str : request.getParameterValues("songIds")) {
				songsID.add(Integer.parseInt(str));
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			return;
		}
		
		// Parse playlistID to number and if not possible send an error
		int playlistID;
		try {
			playlistID = Integer.parseInt(playlistIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			return;
		}
		
		SongDAO songDao = new SongDAO(this.connection);
		try {
			songDao.addSongsToPlaylist(songsID, playlistID, u.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't add song to the playlist");
			return;
		}
		
		String path = getServletContext().getContextPath() + ("/GetPlaylist?playlistId=" + playlistIdString) ;
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
