package com.jozibear247_cab.driver.model;

import java.io.Serializable;

public class User implements Serializable {

	private int userId, emailActivation;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public int getEmailActivation() {
		return emailActivation;
	}

	public void setEmailActivation(int emailActivation) {
		this.emailActivation = emailActivation;
	}

	private String fname, lname, contact, address, bio, zipcode, email,
			picture,timezone,
			make, model, city, reg_no, color, picture_car;

	public void setMake(String make) {this.make = make;}
	public void setModel(String model) {this.model = model;}
	public void setColor(String color) {this.color = color;}
	public void setCity(String city) {this.city = city;}
	public void setRegno(String reg_no) {this.reg_no = reg_no;}
	public void setPictureCar(String picture_car) {this.picture_car = picture_car;}

	public String getMake(){ return make;}
	public String getModel(){ return model;}
	public String getCity(){ return city;}
	public String getColor(){ return color;}
	public String getRegno(){ return reg_no;}
	public String getPictureCar() { return picture_car;}
}
