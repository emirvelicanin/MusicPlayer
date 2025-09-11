package it.polimi.tiw.playlist.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import it.polimi.tiw.playlist.beans.User;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/AddSong")
@MultipartConfig(
		fileSizeThreshold = 10000,
		maxFileSize = 1024 * 1024 * 1000,
		maxRequestSize = 1024 * 1024 * 10000
		)
public class AddSong extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public AddSong() {
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
		
		String songTitle = request.getParameter("songTitle");
		String genre = request.getParameter("genre");
		String selectedAlbum = request.getParameter("album");
		String albumTitle = request.getParameter("newAlbumTitle");
		String singer = request.getParameter("newSinger");
		String yearString = request.getParameter("newPublicationYear");
		Part albumImagePart = request.getPart("newAlbumImage");
		Part audioPart = request.getPart("audioFile");
		InputStream albumImage;
		InputStream audioFile;
	
		
		//can't add a existing album and compile for a new album 
		boolean selectedExisting = selectedAlbum != null && !selectedAlbum.isEmpty();
		boolean filledSomeNewAlbum = 
			    (albumTitle != null && !albumTitle.isEmpty()) ||  
			    (singer != null && !singer.isEmpty()) ||         
			    (yearString != null && !yearString.isEmpty()) ||
			    (albumImagePart != null && albumImagePart.getSize() > 0); 
		 
		if ((selectedExisting &&  filledSomeNewAlbum ) || (!selectedExisting && !filledSomeNewAlbum)) {
		        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You must either select an existing album OR insert all the new album details, not both.");
		        return;
		   }
		
		try {
			// Gets an binary stream of the audio file
			audioFile = audioPart.getInputStream();			
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid  Audio file");
			return;
		}
		
		// Check parameters are present
		if(songTitle == null || songTitle.isEmpty() || genre == null || genre.isEmpty() || audioFile == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "1  Missing parameters");
			return;
		}
		
		// Determines the MIME type of the uploaded file and validate the file type
		String audioMimeType = getServletContext().getMimeType(audioPart.getSubmittedFileName());
		
		if(audioMimeType == null || !audioMimeType.startsWith("audio")) {
			audioFile.close();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid audio");
			return;
			}
	
		//ADD THE SONGS IN A EXISTING ALBUM 
		if(selectedExisting) {
			int albumID;
			try {
				albumID = Integer.parseInt(selectedAlbum);
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters for existing album ID");
				return;
			}
			
			SongDAO songDAO = new SongDAO(this.connection);
			try{
				songDAO.uploadSongExistingAlbum(songTitle, albumID, genre, audioFile, u.getUsername());
				audioFile.close();
				
				String path = getServletContext().getContextPath()+ "/GoToHomePage";
				response.sendRedirect(path);
				return;
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in uploading song");
				return;
			}
			
		}

		// CREATE A NEW ALBUM AND THEN  ADD THE SONG
		int year;
		try {
			year = Integer.parseInt(yearString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters for Year of Publication");
			return;
			}
		
		String imageMimeType = getServletContext().getMimeType(albumImagePart.getSubmittedFileName());
			
		if(imageMimeType == null || !imageMimeType.startsWith("image")) {
			audioFile.close();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image");
			return;
		}
				
		try {
			albumImage = albumImagePart.getInputStream();
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Image file");
			return;
			}
		
		if(albumTitle == null || albumTitle.isEmpty() || singer == null || singer.isEmpty()
				|| yearString == null || yearString.isEmpty() || albumImage == null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		SongDAO songDAO = new SongDAO(this.connection);
		try {
			//check Album with same Title exist
			if(songDAO.existAlbum(albumTitle,u.getUsername())) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in uploading song because Already exist an album with this Title");
				return;
			}
			
			songDAO.uploadSongNotExistingAlbum(songTitle, albumTitle,singer,year, genre, albumImage, audioFile, u.getUsername());	
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure in uploading song --> try to change Song Title");
			return;
		}	
		albumImage.close();
		audioFile.close();
		
		String path = getServletContext().getContextPath()+ "/GoToHomePage";
		response.sendRedirect(path);
	}
	
	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}