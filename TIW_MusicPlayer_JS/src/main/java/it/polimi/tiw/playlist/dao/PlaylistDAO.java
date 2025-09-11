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
		
		int playlistID;
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setString(1, title);
			pstatement.setDate(2, new Date((new java.util.Date()).getTime()));
			pstatement.setString(3, username);
			pstatement.executeUpdate();
				
			ResultSet result = pstatement.getGeneratedKeys();
			result.next();
			playlistID = result.getInt(1);
				
			result.close();
			}
			
		if(songList.size() > 0) {
			SongDAO songDao = new SongDAO(this.connection);
			try {
				for(int songId : songList) {
					songDao.addSongToPlaylist(songId, playlistID, username);
				}
				connection.commit();
			} catch (SQLException e) {
				connection.rollback(); //ROLLBACK ALL previous operations if there is an error 
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
	public void checkPlaylistOwner(int playlistID, String username) throws SQLException {
		String query = "SELECT COUNT(*) AS total FROM Playlist WHERE ID = ? AND Username_User = ?";
		
		int count;
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistID);
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
		
		String query = "(SELECT id, SongTitle FROM Song WHERE Username_User = ?) EXCEPT (SELECT ID, SongTitle FROM Song WHERE ID IN (SELECT ID_Song FROM PlaylistSong WHERE ID_Playlist = ?))";
		
		ArrayList<Song> songs = new ArrayList<>();
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setInt(2, playlistId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {		
					Song song = new Song();
					song.setID(result.getInt("ID"));
					song.setSongTitle(result.getString("SongTitle"));
					song.setUsernameUser(username);
					songs.add(song);
				}
			}
		}
		return songs;
	}
	
	// get song of the PlayList with offset
	public ArrayList<Song> getSongsFiltered(int playlistID, String username, int offset) throws SQLException {
		this.checkPlaylistOwner(playlistID, username);
		
		String query = "SELECT song.ID, song.songTitle, album.imagePath, album.title, "
	             + "album.Singer, album.PublicationYear FROM song AS song "
	             + "LEFT JOIN album AS album ON song.albumID = album.ID "
	             + "WHERE song.id IN (SELECT ID_song FROM playlistsong"
	             + "WHERE ID_playlist = ? AND username_user = ?)"
	             + "ORDER BY album.publicationYear DESC LIMIT 5 OFFSET ?";
		
		ArrayList<Song> songs = new ArrayList<>();
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistID);
			pstatement.setString(2, username);
			pstatement.setInt(3, offset);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setID(result.getInt("ID"));
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
	
	//get all Song of the PlayList
	public ArrayList<Song> getSongs(int playlistId, String username) throws SQLException {
		this.checkPlaylistOwner(playlistId, username);
		
		String query = "SELECT song.ID, song.SongTitle, album.imagePath, album.Title, "
	             + "album.Singer, album.PublicationYear, ps.Position FROM SONG AS song "
	             + "LEFT JOIN album AS album ON song.albumID = album.ID "
	             + "LEFT JOIN PlaylistSong AS ps ON song.ID = ps.ID_Song AND ps.ID_Playlist = ? AND ps.Username_User = ?"
	             + "WHERE song.ID IN (SELECT id_song FROM playlistsong "
	             + "WHERE ID_playlist = ? AND username_user = ?) "
	             + "ORDER BY ps.position ASC, album.PublicationYear DESC";
		

		ArrayList<Song> songs = new ArrayList<>();
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistId);
			pstatement.setString(2, username);
			pstatement.setInt(3, playlistId);
			pstatement.setString(4, username);

			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setID(result.getInt("ID"));
					song.setSongTitle(result.getString("songTitle"));
					song.setAlbumImage(Base64.getEncoder().encodeToString(result.getBytes("imagePath")));
					song.setPublicationYear(result.getInt("PublicationYear"));
					song.setUsernameUser(username);
					songs.add(song);
				}
			}
		}
		return songs;
	}
	
	
	public void setPlaylistOrder(int playlistID, ArrayList<Integer> songsID, String username) throws SQLException {
		this.checkPlaylistOwner(playlistID, username);
		
		String query = "UPDATE playlistSong SET position = ? WHERE ID_SONG = ? AND Username_User = ? AND ID_Playlist = ?";
		
		connection.setAutoCommit(false);
		try {
			for(int i = 0; i < songsID.size(); i++) {
				try(PreparedStatement pstatement = connection.prepareStatement(query);) {
					pstatement.setInt(1, i);
					pstatement.setInt(2, songsID.get(i));
					pstatement.setString(3, username);
					pstatement.setInt(4, playlistID);
					pstatement.executeUpdate();
				}
			}
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}
	
	public void removeSongFromPlaylist(int songID, int playlistID, String username) throws SQLException {
		this.checkPlaylistOwner(playlistID, username);
		
		SongDAO songDao = new SongDAO(this.connection);
		songDao.checkSongOwner(songID, username);
		
		String query = "DELETE FROM  PlaylistSong WHERE ID_Song = ? AND ID_Playlist = ? AND Username_User= ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, songID);
			pstatement.setInt(2, playlistID);
			pstatement.setString(3, username);
			pstatement.executeUpdate();
		}
	}
	
	public void deletePlaylist(int playlistID, String username) throws SQLException {
		this.checkPlaylistOwner(playlistID, username);

		String query = "DELETE FROM playlistSong WHERE ID_Playlist= ? AND Username_User= ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistID);
			pstatement.setString(2, username);
			pstatement.executeUpdate();
		}
		
		query = "DELETE FROM Playlist WHERE ID = ? AND Username_User= ?";	
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, playlistID);
			pstatement.setString(2, username);
			pstatement.executeUpdate();
		}
		
	}
	
	

	
}