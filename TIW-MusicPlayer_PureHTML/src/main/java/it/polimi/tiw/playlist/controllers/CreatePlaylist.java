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
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;


@WebServlet("/CreatePlaylist")
public class CreatePlaylist extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public CreatePlaylist() {
	    super();
	}

	public void init() throws ServletException {
		this.connection = ConnectionHandler.getConnection(getServletContext());
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
			
		String playlistName = request.getParameter("playlistname");
		
		// Check parameters are present
		if(playlistName == null || playlistName.isEmpty() || request.getParameterValues("songIds") == null) {
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
			
		PlaylistDAO playlistDAO = new PlaylistDAO(this.connection);
		try {
			playlistDAO.createPlaylist(playlistName, songsID, u.getUsername());
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't create playlist --> try to change Playlist Title");
				return;
			}
		
			String path = getServletContext().getContextPath() + "/GoToHomePage";
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