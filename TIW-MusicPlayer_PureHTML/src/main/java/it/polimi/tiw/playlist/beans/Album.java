package it.polimi.tiw.playlist.beans;

public class Album {
	private int ID;
	private String title;
	private String imagePath;
	private String singer;
	private int publicationYear;

	public Album() {}
	
	public int getID() {
		return this.ID;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getImagePath() {
		return this.imagePath;
	}
	
	public String getSinger() {
		return this.singer;
	}
	
	public int getPublicationYear() {
		return this.publicationYear;
	}
	
	public void setID(int ID) {
		this.ID = ID;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	public void setSinger(String singer) {
		this.singer = singer;
	}
	
	public void setPublicationYear(int publicationYear) {
		this.publicationYear = publicationYear;
	}
}
