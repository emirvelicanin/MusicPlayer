package it.polimi.tiw.playlist.beans;

import java.sql.Date;

public class Playlist {
	private int ID;
	private String title;
	private Date creationDate;
	private String usernameUser;

	public Playlist() {}
	
	public int getID() {
		return this.ID;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public Date getCreationDate() {
		return this.creationDate;
	}
	
	public String getUsernameUser() {
		return this.usernameUser;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public void setUsernameUser(String usernameUser) {
		this.usernameUser = usernameUser;
	}
}