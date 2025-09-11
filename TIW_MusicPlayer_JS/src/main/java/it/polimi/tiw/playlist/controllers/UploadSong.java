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

@WebServlet("/UploadSong")
@MultipartConfig(
		fileSizeThreshold = 10000,
		maxFileSize = 1024 * 1024 * 1000,
		maxRequestSize = 1024 * 1024 * 10000
		)
public class UploadSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public UploadSong() {
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
		
		String songTitle = request.getParameter("song_title");
		String genre = request.getParameter("genre");
		String selectedAlbum = request.getParameter("album_select");
		String albumTitle = request.getParameter("new_album_title");
		String singer = request.getParameter("new_album_singer");
		String yearString = request.getParameter("new_album_publication_year");
		Part albumImagePart = request.getPart("new_album_image");
		Part audioPart = request.getPart("audio_file");
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("You must either select an existing album OR insert all the new album details, not both.");    
		    return;
		   }
		
		try {
			// Gets an binary stream of the audio file
			audioFile = audioPart.getInputStream();			
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid  Audio file");    
		    return;
		}
		
		// Check parameters are present
		if(songTitle == null || songTitle.isEmpty() || genre == null || genre.isEmpty() || audioFile == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters");    
			return;
		}
			
		// Determines the MIME type of the uploaded file and validate the file type
		String audioMimeType = getServletContext().getMimeType(audioPart.getSubmittedFileName());
		
		if(audioMimeType == null || !audioMimeType.startsWith("audio")) {
			audioFile.close();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid audio");
			return;
			}
	
		//ADD THE SONGS IN A EXISTING ALBUM 
		if(selectedExisting) {
			int albumID;
			try {
				albumID = Integer.parseInt(selectedAlbum);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid parameters for existing album ID");
				return;
			}
			
			SongDAO songDAO = new SongDAO(this.connection);
			try{
				songDAO.uploadSongExistingAlbum(songTitle, albumID, genre, audioFile, user.getUsername());
				audioFile.close();
				return;
				}
			catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Failure in uploading song... TRY TO change SongTitle");
				return;
			}
			
		}
		
		// CREATE A NEW ALBUM AND THEN  ADD THE SONG
		int year;
		try {
			year = Integer.parseInt(yearString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid parameters for Year of Publication");
			e.printStackTrace();
			return;
			}
		
		String imageMimeType = getServletContext().getMimeType(albumImagePart.getSubmittedFileName());
			
		if(imageMimeType == null || !imageMimeType.startsWith("image")) {
			audioFile.close();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid image");
			return;
		}
				
		try {
			albumImage = albumImagePart.getInputStream();
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid Image file");
			return;
			}
		
		if(albumTitle == null || albumTitle.isEmpty() || singer == null || singer.isEmpty()
				|| yearString == null || yearString.isEmpty() || albumImage == null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters");
			return;
		}
		
		
		SongDAO songDAO = new SongDAO(this.connection);
		
		try {
			if(songDAO.existAlbum(albumTitle,user.getUsername())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Failure in uploading song because Already exist an album with this Title");	
			return;
			}
			
			songDAO.uploadSongNotExistingAlbum(songTitle, albumTitle,singer,year, genre, albumImage, audioFile, user.getUsername());
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Failure in uploading song...TRY TO change SongTitle");
			e.printStackTrace();
			return;
		}	
		
		albumImage.close();
		audioFile.close();
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
