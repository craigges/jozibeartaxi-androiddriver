/**
 * 
 */
package com.jozibear247_cab.driver.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

/**
 * @author Kishan H Dhamat
 * 
 */
public class History {

	private int id;
	private String date, distance, time, basePrice, distanceCost, timecost,
			total, firstName, lastName, phone, email, picture, bio, currency,
			primary_amount, secoundry_amount;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the date
	 */
	public String getDate() {

		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the diatance
	 */
	public String getDistance() {
		return distance;
	}

	/**
	 * @param diatance
	 *            the diatance to set
	 */
	public void setDistance(String distance) {
		this.distance = distance;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the basePrice
	 */
	public String getBasePrice() {
		return basePrice;
	}

	/**
	 * @param basePrice
	 *            the basePrice to set
	 */
	public void setBasePrice(String basePrice) {
		this.basePrice = basePrice;
	}

	/**
	 * @return the distanceCost
	 */
	public String getDistanceCost() {
		return distanceCost;
	}

	/**
	 * @param distanceCost
	 *            the distanceCost to set
	 */
	public void setDistanceCost(String distanceCost) {
		this.distanceCost = distanceCost;
	}

	/**
	 * @return the timecost
	 */
	public String getTimecost() {
		return timecost;
	}

	/**
	 * @param timecost
	 *            the timecost to set
	 */
	public void setTimecost(String timecost) {
		this.timecost = timecost;
	}

	/**
	 * @return the total
	 */
	public String getTotal() {
		return total;
	}

	/**
	 * @param total
	 *            the total to set
	 */
	public void setTotal(String total) {
		this.total = total;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 *            the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the picture
	 */
	public String getPicture() {
		return picture;
	}

	/**
	 * @param picture
	 *            the picture to set
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}

	/**
	 * @return the bio
	 */
	public String getBio() {
		return bio;
	}

	/**
	 * @param bio
	 *            the bio to set
	 */
	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPrimary_amount() {
		return primary_amount;
	}

	public void setPrimary_amount(String primary_amount) {
		this.primary_amount = primary_amount;
	}

	public String getSecoundry_amount() {
		return secoundry_amount;
	}

	public void setSecoundry_amount(String secoundry_amount) {
		this.secoundry_amount = secoundry_amount;
	}
}
