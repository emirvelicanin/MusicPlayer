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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/GetPlaylist")
public class GetPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private JakartaServletWebApplication application;
	private TemplateEngine templateEngine;

	public GetPlaylist() {
		super();
	}

	public void init() throws ServletException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(this.application);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
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
		
		int playlistId;
		String playlistIdString = request.getParameter("playlistId");
		
		// Check parameters are present
		if(playlistIdString == null || playlistIdString.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		try {
			playlistId = Integer.parseInt(playlistIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
			return;
		}
		
		int page;
		String pageString = request.getParameter("page");
		if(pageString == null || pageString.isEmpty()) {
			page = 1;
		}
		else {
			try {
				page = Integer.parseInt(pageString);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid  Page Parameters");
				return;
			}
		}
		
		//check page number compare to number  songs of Playlist
		PlaylistDAO playlistDao = new PlaylistDAO(this.connection);
		int count = 0;
		try {
			count = playlistDao.countSongs(playlistId, user.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get song count from the database");
			return;
		}
		
		if(page > ((count-1)/5)+1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid page");
			return;
		}
		
		//Get the list of songs from the database, filtering them to get at most 5 of the current page
		ArrayList<Song> dbSongs = null;
		try {
			dbSongs = playlistDao.getSongsFiltered(playlistId, user.getUsername(), (page - 1) * 5);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get songs from the database");
			return;
		}
		
		String playlistName = null;
		try {
			playlistName = playlistDao.getPlaylistTitle(playlistId, user.getUsername());
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get playlist name from database");
			return;
		}
		
		//set variable noSongs
		boolean noSongs = false;
		if(dbSongs == null || dbSongs.isEmpty()) {
			noSongs = true;
		}
		
		//set variable for next and previous Button
		boolean lastPage = false;
		if(dbSongs.size() < 5 || page * 5 == count) {
			lastPage = true;
		}
		
		boolean firstPage = false;
		if(page - 1 == 0) {
			firstPage = true;
		}
		
		// excluded songs
		ArrayList<Song> excludedSongs = null;
		try {
			excludedSongs = playlistDao.getSongsNotInPlaylist(user.getUsername(), playlistId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Can't get songs from the database");
			return;
		}
		
		//Set context variables and process the template
		String path = "/WEB-INF/PlaylistPage.html";
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		final WebContext ctx = new WebContext(webExchange, request.getLocale());
		ctx.setVariable("songsInPlaylist", dbSongs);
		ctx.setVariable("firstPage", firstPage);
		ctx.setVariable("lastPage", lastPage);
		ctx.setVariable("page", page);
		ctx.setVariable("songsNotInPlaylist", excludedSongs);
		ctx.setVariable("playlistId", request.getParameter("playlistId"));
		ctx.setVariable("Title", playlistName);
		ctx.setVariable("noSongs", noSongs);
	
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
