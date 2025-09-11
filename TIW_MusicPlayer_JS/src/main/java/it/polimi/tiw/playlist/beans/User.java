package it.polimi.tiw.playlist.beans;


public class User {
	private String username;
	private String password;
	private String name;
	private String surname;
	
	public User(String username, String password, String name, String surname){
		this.username = username;
		this.password = password;
		this.name = name;
		this.surname = surname; 
	}
	
	public User() {}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSurname() {
		return this.surname;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPasssword(String password) {
		this.password = password;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}
}