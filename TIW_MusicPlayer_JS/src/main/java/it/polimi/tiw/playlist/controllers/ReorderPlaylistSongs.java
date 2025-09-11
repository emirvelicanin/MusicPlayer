package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

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

@WebServlet("/ReorderPlaylistSongs")
@MultipartConfig
public class ReorderPlaylistSongs extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

    public ReorderPlaylistSongs() {
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
		
		String songIdsString = request.getParameter("songIds");
		String playlistIdString = request.getParameter("playlistId");

		if(songIdsString == null || songIdsString.isEmpty() || playlistIdString == null || playlistIdString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters");
			return;
		}
		
		int playlistId;
		String[] strings;
		ArrayList<Integer> songIds = new ArrayList<>();
		try {
			strings = songIdsString.split(",");
			for(String string : strings) {
				songIds.add(Integer.parseInt(string));
			}
			playlistId = Integer.parseInt(playlistIdString);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid parameters");
			return;
		}
		
		PlaylistDAO playlistDAO = new PlaylistDAO(this.connection);
		try {
			playlistDAO.setPlaylistOrder(playlistId, songIds, user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Error in setting playlist order");
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
