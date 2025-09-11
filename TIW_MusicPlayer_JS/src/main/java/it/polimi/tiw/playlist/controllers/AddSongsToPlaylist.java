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
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/AddSongsToPlaylist")
@MultipartConfig
public class AddSongsToPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public AddSongsToPlaylist() {
        super();
    }

    public void init() throws ServletException {
    	this.connection = ConnectionHandler.getConnection(getServletContext());
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
		
		String playlistIDString = request.getParameter("playlistID");
		
		// Check parameters are present
		if(playlistIDString== null || playlistIDString.isEmpty() || request.getParameterValues("songsID") == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Missing parameters");
			return;
		}
		
		// Parse SongsID to number and if not possible send an error
		ArrayList<Integer> songsID = new ArrayList<>();
		try {
			for(String str : request.getParameterValues("songsID")) {
				songsID.add(Integer.parseInt(str));
			}
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid Songs ID Parameters");
			return;
		}
		
		// Parse playlistID to number and if not possible send an error
		int playlistID;
		try {
			playlistID = Integer.parseInt(playlistIDString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid Playlist ID Parameters");			
			return;
		}
		
		SongDAO songDAO = new SongDAO(this.connection);
		try {
			songDAO.addSongsToPlaylist(songsID, playlistID, user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Can't add song to the playlist");	
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