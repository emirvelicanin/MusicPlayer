package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import it.polimi.tiw.playlist.beans.Album;
import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void createUser(String username , String password, String name, String surname) throws SQLException{
		
		if(findUser(username) == true) throw new SQLException("Username is taken");
		
		String query = "INSERT INTO USER (Username,Password,Name,Surname) VALUES(?,?,?,?)";
		
		try(PreparedStatement pStatement = connection.prepareStatement(query)) {
			pStatement.setString(1 , username);
			pStatement.setString(2 , password);
			pStatement.setString(3 , name);
			pStatement.setString(4 , surname);
			pStatement.executeUpdate();
		}
	}
	
	//search in the DB is the user exist or if the userName is already used by an other user
	public boolean findUser(String username) throws SQLException{
		boolean result = false;
		String query = "SELECT Username FROM USER WHERE Username = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query)){
			pstatement.setString(1,username);
			try (ResultSet resultSet = pstatement.executeQuery()) {
				if(resultSet.next()) result = true;
			}
		}
		
		return result;
	}
	
	//Method that verify if userName and Password, inserted during the login, are correct
	public User checkAuthentication(String username, String password) throws SQLException{
		String query ="SELECT * FROM USER WHERE Username = ? AND Password = ?";
		
		try (PreparedStatement pStatement = connection.prepareStatement(query)){		
			pStatement.setString(1 , username);
			pStatement.setString(2 , password);
			
			try(ResultSet resultSet = pStatement.executeQuery()){
				if(resultSet.next()) {
					return new User(resultSet.getString("Username"), resultSet.getString("Password"),  resultSet.getString("Name"),  resultSet.getString("Surname"));
				}else {
					return null;
				}
			}
		}
	}

	
	public ArrayList<Song> getAllUserSongs(String username, boolean getAlbumCovers) throws SQLException {
		
		String query = "SELECT song.ID, song.SongTitle, album.ImagePath "
				+ "FROM Song LEFT JOIN Album album ON song.AlbumID = album.ID "
				+ "WHERE song.Username_User = ?";
		
		ArrayList<Song> songs = new ArrayList<>();
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Song song = new Song();
					song.setID(result.getInt("ID"));
					song.setSongTitle(result.getString("SongTitle"));
					song.setUsernameUser(username);
					if(getAlbumCovers) {
						song.setAlbumImage(Base64.getEncoder().encodeToString(result.getBytes("imagePath")));						
					}
					songs.add(song);
				}
			}
		}
		return songs;
	}
	
	public ArrayList<Playlist> findPlaylists(String username) throws SQLException {
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();
		
		String query = "SELECT ID, Title, CreationDate FROM playlist WHERE Username_User = ? ORDER BY playlist.CreationDate DESC";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Playlist playlist = new Playlist();
					playlist.setID(result.getInt("ID"));
					playlist.setUsernameUser(username);
					playlist.setTitle(result.getString("Title"));
					playlist.setCreationDate(result.getDate("CreationDate"));
					playlists.add(playlist);
				}
			}
		}
		return playlists;
	}
	
	public ArrayList<Album> getAllAlbums(String username) throws SQLException {
		String query = "SELECT ID,Title, Singer FROM Album WHERE Username_User = ? ";
		
		ArrayList<Album> albums = new ArrayList<>();
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {					
					Album album = new Album();
					album.setID(result.getInt("ID"));
					album.setTitle(result.getString("Title"));
					album.setSinger(result.getString("Singer"));
					albums.add(album);
				}
			}
		}
		return albums;
	}
	
	public void deleteAccount(String username) throws SQLException {
		
		//delete all PlaylistSong of User
		String query = "DELETE FROM PlaylistSong WHERE Username_User= ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.executeUpdate();
		}
		
		//delete all Album of User
		query = "DELETE FROM Album WHERE Username_User= ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.executeUpdate();
		}
		
		//delete all Song of User
		query = "DELETE FROM Song WHERE Username_User= ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.executeUpdate();
		}
		
		//delete all PlayList of User
		query = "DELETE FROM Playlist WHERE Username_User= ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.executeUpdate();
		}
		
		//delete all User of User
		query = "DELETE FROM User WHERE Username = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.executeUpdate();
		}
	}
}	
	