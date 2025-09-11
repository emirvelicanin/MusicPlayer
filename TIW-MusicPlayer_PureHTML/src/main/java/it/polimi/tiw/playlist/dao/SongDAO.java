package it.polimi.tiw.playlist.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;

import it.polimi.tiw.playlist.beans.Song;

public class SongDAO {
	private Connection connection;
	
	public SongDAO(Connection connection) {
		this.connection = connection;
	}

	//Create the Album then Song
	public void uploadSongNotExistingAlbum(String songTitle, String albumTitle, String singer, int publicationYear, 
	        String genre, InputStream imagePath, InputStream songFilePath, String usernameUser) throws SQLException {
	    
	    String albumQuery = "INSERT INTO Album (Title, Username_User, ImagePath, Singer, PublicationYear) VALUES (?, ?, ?, ?, ?)";
	    
	    String songQuery = "INSERT INTO Song (SongTitle, Genre, SongFilePath, Username_User, AlbumID) VALUES (?, ?, ?, ?, ?)";  
	    
	    connection.setAutoCommit(false);
	    
	    try (PreparedStatement albumStatement = connection.prepareStatement(albumQuery, Statement.RETURN_GENERATED_KEYS);
	         PreparedStatement songStatement = connection.prepareStatement(songQuery)) {
	        
	        albumStatement.setString(1, albumTitle);
	        albumStatement.setString(2, usernameUser);
	        albumStatement.setBinaryStream(3, imagePath);
	        albumStatement.setString(4, singer);
	        albumStatement.setInt(5, publicationYear);
	        albumStatement.executeUpdate();
	        
	        int albumId;
	        try (ResultSet generatedKeys = albumStatement.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                albumId = generatedKeys.getInt(1);  // ID ALBUM
	            } else {
	                throw new SQLException("Failed to get album ID, no keys generated.");
	            }
	        }
	        
	        songStatement.setString(1, songTitle);
	        songStatement.setString(2, genre);
	        songStatement.setBinaryStream(3, songFilePath);
	        songStatement.setString(4, usernameUser);
	        songStatement.setInt(5, albumId);  
	        songStatement.executeUpdate();
	        
	        connection.commit();
	        
	    } catch (SQLException e) {
	        connection.rollback();  
	        throw e;
	    } finally {
	        connection.setAutoCommit(true); 
	    }
	}
	
	public void uploadSongExistingAlbum(String songTitle, int albumID, String genre, InputStream songFilePath, String usernameUser) throws SQLException {
		String songQuery = "INSERT INTO Song (SongTitle, Genre, SongFilePath, Username_User, AlbumID) VALUES (?,?,?,?,?)";

		try (PreparedStatement songStatement = connection.prepareStatement(songQuery)) {
			    songStatement.setString(1, songTitle);
			    songStatement.setString(2, genre);
			    songStatement.setBinaryStream(3, songFilePath);
			    songStatement.setString(4,usernameUser);
			    songStatement.setInt(5, albumID);
			    songStatement.executeUpdate();
		}
			
	}
	
	//Check Album Title  isn't used by User 
	public boolean existAlbum(String albumTitle, String username) throws SQLException{
	    String query = "SELECT COUNT(*) AS count FROM Album WHERE Title = ? AND Username_user = ? ";
	    try (PreparedStatement pstatement = connection.prepareStatement(query)) {
	        pstatement.setString(1, albumTitle);
	        pstatement.setString(2, username);

	        try (ResultSet result = pstatement.executeQuery()) {
	            if (result.next()) {
	                return result.getInt("count") > 0;
	            }
	        }
	    }
	    return false;
	}
	
	public void addSongToPlaylist(int songID, int playlistID, String username) throws SQLException {
		//Check if the song and the playlist actually belong to the user
		PlaylistDAO pDao = new PlaylistDAO(this.connection);
		pDao.checkPlaylistOwner(playlistID, username);
		this.checkSongOwner(songID, username);
		
		//Check if the song is already in the Playlist
		String query = "SELECT COUNT(*) AS total FROM PlaylistSong WHERE ID_Playlist = ? AND ID_Song = ?";
		
		int count = 0;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistID);
			pstatement.setInt(2, songID);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		if(count > 0) {
			throw new SQLException("Song already in playlist");
		}
		
		query = "INSERT INTO PlaylistSong (ID_Song, ID_Playlist, Username_User) VALUES (?,?,?)";
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songID);
			pstatement.setInt(2, playlistID);
			pstatement.setString(3, username);
			pstatement.executeUpdate();
		}
	}
	
	//Add List of Songs in a PlayList
	public void addSongsToPlaylist(ArrayList<Integer> songList, int playlistId, String username) throws SQLException {
		connection.setAutoCommit(false);
		if(songList.size() > 0) {
			try {
				for(int songId : songList) {
					this.addSongToPlaylist(songId, playlistId, username);
				}
				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				throw e;
			} finally {
				connection.setAutoCommit(true);
			}
		}
		else {
			connection.setAutoCommit(true);
		}
	}
	
	public Song getSong(int songId, String username) throws SQLException {
		this.checkSongOwner(songId, username);
		Song song = new Song();
		
		String query = "SELECT song.songTitle, album.singer, album.publicationYear, song.genre , song.SongFilePath," +
	               "album.Title AS album_title, album.ImagePath " +
	               "FROM Song song " +
	               "LEFT JOIN Album album ON song.albumID = album.ID " +
	               "WHERE song.ID = ? AND song.username_User = ?";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.setString(2, username);
			
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					song.setId(songId);
					song.setSongTitle(result.getString("SongTitle"));
					song.setAlbumTitle(result.getString("album_title"));
					song.setAlbumImage(Base64.getEncoder().encodeToString(result.getBytes("imagePath")));
					song.setSinger(result.getString("singer"));
					song.setPublicationYear(result.getInt("PublicationYear"));
					song.setGenre(result.getString("genre"));
					song.setSongFilePath(Base64.getEncoder().encodeToString(result.getBytes("SongFilePath")));
					song.setUsernameUser(username);
				}
			}
		}
		return song;
	}
	
	public void checkSongOwner(int songId, String username) throws SQLException {
		int count = 0;
		String query = "SELECT COUNT(*) AS total FROM Song WHERE ID = ? AND Username_User = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.setString(2, username);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		if(count <= 0) {
			throw new SQLException("Invalid song");
		}		
	}
	public void deleteSong(int songId, String username) throws SQLException {
		this.checkSongOwner(songId, username);
		
		//Delete song from all PlayLists
		String query = "DELETE FROM PlaylistSong WHERE ID_Song = ?";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.executeUpdate();
		}
		
		//Delete Song Info
		query = "DELETE FROM MusicPlaylistDB.Song WHERE ID = ?";	
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.executeUpdate();
		}
	}	
}
