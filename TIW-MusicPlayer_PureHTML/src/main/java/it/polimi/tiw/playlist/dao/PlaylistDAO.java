package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Base64;

import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.Song;



public class PlaylistDAO {
	private Connection connection;
	
	public PlaylistDAO(Connection connection) {
		this.connection = connection;
	}
	
	//Create a new PlayList and added all Songs selected
	public void createPlaylist(String title, ArrayList<Integer> songList, String username) throws SQLException {
		connection.setAutoCommit(false);
			
		String query = "INSERT INTO Playlist (Title, CreationDate, Username_User) VALUES (?,?,?)";
		
		int playlistId;
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setString(1, title);
			pstatement.setDate(2, new Date((new java.util.Date()).getTime()));
			pstatement.setString(3, username);
			pstatement.executeUpdate();
				
			ResultSet result = pstatement.getGeneratedKeys();
			result.next();
			playlistId = result.getInt(1);
				
			result.close();
			}
			
		if(songList.size() > 0) {
			SongDAO songDao = new SongDAO(this.connection);
			try {
				for(int songId : songList) {
					songDao.addSongToPlaylist(songId, playlistId,username);
				}
				connection.commit();
			} catch (SQLException e) {
				connection.rollback();//ROLLBACK ALL previous operations if there is an error 
				throw e;
			} finally {
				connection.setAutoCommit(true);
			}
		}else {
			connection.commit();
			connection.setAutoCommit(true);
		}
	}
	
	// Return list of PlayList of the user
	public ArrayList<Playlist> findPlaylist(String username) throws SQLException{
		String query = "SELECT * FROM playlist WHERE Username_User = ? ORDER BY CreationDate DESC";
		
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();
		
		try (PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Playlist playlist = new Playlist();
					playlist.setID(result.getInt("ID"));
					playlist.setTitle(result.getString("Title"));
					playlist.setCreationDate(result.getDate("CreationDate"));
					playlist.setUsernameUser(username);
					playlists.add(playlist);
				}
			}
		}
		return playlists;
	}
	
	//check the owner of the PlayList --> if it isn't the owner throw SQLException
	public void checkPlaylistOwner(int playlistId, String username) throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM Playlist WHERE ID = ? AND Username_User = ?";
		
		int count;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setString(2, username);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		if(count <= 0) {
			throw new SQLException("Invalid playlist");
		}
	}
	
	public String getPlaylistTitle(int playlistId, String username) throws SQLException {
		this.checkPlaylistOwner(playlistId, username);
		
		String query = "SELECT Title FROM Playlist WHERE ID = ? AND Username_User = ?";
		
		String title = null;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setString(2, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {					
					title = result.getString("Title");
				}
			}
		}
		return title;
	}	
	
	public int countSongs (int playlistId, String username) throws SQLException {
		this.checkPlaylistOwner(playlistId, username);
		
		String query = "SELECT COUNT(*) AS total FROM PlaylistSong WHERE ID_playlist = ?";
		
		int count;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				count = result.getInt("total");
			}
		}
		return count;
	}
	
	public ArrayList<Song> getSongsNotInPlaylist(String username, int playlistId) throws SQLException {
		this.checkPlaylistOwner(playlistId, username);
		
		String query = "(SELECT id, SongTitle FROM Song WHERE Username_User = ?) "
				+ "EXCEPT (SELECT ID, SongTitle FROM Song WHERE ID IN (SELECT ID_Song FROM PlaylistSong WHERE ID_Playlist = ?))";
		
		ArrayList<Song> songs = new ArrayList<>();
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setInt(2, playlistId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setId(result.getInt("ID"));
					song.setSongTitle(result.getString("SongTitle"));
					song.setUsernameUser(username);
					songs.add(song);
				}
			}
		}
		return songs;
	}
	
	// get song of the PlayList with offset
	public ArrayList<Song> getSongsFiltered(int playlistId, String username, int offset) throws SQLException {
		this.checkPlaylistOwner(playlistId, username);
		
		String query = "SELECT song.id, song.songTitle, album.imagePath, album.title, "
	             + "album.Singer, album.PublicationYear FROM song song "
	             + "LEFT JOIN album album ON song.albumID = album.id "
	             + "WHERE song.id IN (SELECT id_song FROM playlistsong "
	             + "WHERE id_playlist = ? AND username_user = ?) "
	             + "ORDER BY album.publicationYear DESC LIMIT 5 OFFSET ?";
		
		ArrayList<Song> songs = new ArrayList<>();
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setString(2, username);
			pstatement.setInt(3, offset);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setId(result.getInt("ID"));
					song.setSongTitle(result.getString("SongTitle"));
					song.setAlbumImage(Base64.getEncoder().encodeToString(result.getBytes("imagePath")));
					song.setPublicationYear(result.getInt("publicationYear"));
					song.setUsernameUser(username);
					songs.add(song);
				}
			}
		}
		return songs;
	}
	
	public void removeSongFromPlaylist(int songId, int playlistId, String username) throws SQLException {
		this.checkPlaylistOwner(playlistId, username);
		SongDAO sDao = new SongDAO(this.connection);
		sDao.checkSongOwner(songId, username);
		
		String query = "DELETE FROM  PlaylistSong WHERE ID_Song = ? AND ID_Playlist = ? AND Username_User= ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songId);
			pstatement.setInt(2, playlistId);
			pstatement.setString(3, username);
			pstatement.executeUpdate();
		}
	}
	
	public void deletePlaylist(int playlistId, String username) throws SQLException {
		this.checkPlaylistOwner(playlistId, username);

		String query = "DELETE FROM playlistSong WHERE ID_Playlist= ? AND Username_User= ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setString(2, username);
			pstatement.executeUpdate();
		}
		
		query = "DELETE FROM Playlist WHERE ID = ? AND Username_User= ?";	
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setString(2, username);
			pstatement.executeUpdate();
		}
	}
}
