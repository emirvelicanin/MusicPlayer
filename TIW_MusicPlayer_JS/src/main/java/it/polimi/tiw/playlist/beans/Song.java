package it.polimi.tiw.playlist.beans;


public class Song{
	private int ID;
	private String songTitle;
	private String genre;
	private String songFilePath;
	private String albumImage;
	private String albumTitle;
	private String usernameUser;
	private int publicationYear;
	private String singer;
	
	public Song() {}
	
	public int getID() {
		return this.ID;
	}
	
	public String getSongTitle() {
		return this.songTitle;
	}
	
	public String getGenre() {
		return this.genre;
	}
	
	public String getSongFilePath() {
		return this.songFilePath;
	}
	
	public String getAlbumImage() {
		return this.albumImage;
	}
	
	public String getAlbumTitle() {
		return this.albumTitle;
	}
	
	public String getUsernameUser() {
		return this.usernameUser;
	}
	
	public int getPublicationYear() {
		return this.publicationYear;
	}
	
	public String getSinger() {
		return this.singer;
	}

	public void setID(int ID) {
		this.ID = ID;
	}
	
	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}
	
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public void setSongFilePath(String songFilePath) {
		this.songFilePath = songFilePath;
	}
	
	public void setAlbumImage(String albumImage) {
		this.albumImage = albumImage;
	}
	
	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}
	
	public void setUsernameUser(String usernameUser) {
		this.usernameUser = usernameUser;
	}
	
	public void setPublicationYear(int publicationYear) {
		this.publicationYear = publicationYear;
	}
	
	public void setSinger(String singer) {
		this.singer = singer;
	}
}