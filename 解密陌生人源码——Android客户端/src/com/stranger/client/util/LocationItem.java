package com.stranger.client.util;

import org.jivesoftware.smack.packet.DiscoverItems.Item;

import android.R.bool;

public class LocationItem extends Item {

	private String username, updatetime, longitude, latitude;
	private String gender, nickname, jid;

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public LocationItem(String entityID) {
		super(entityID);
	}

	public LocationItem() {
		super(null);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	@Override
	public String toXML() {

		StringBuilder buffer = new StringBuilder();
		buffer.append("<item username=\"" + username + "\"");
		buffer.append(" updatetime=\"" + updatetime + "\"");
		buffer.append(" longitude=\"" + longitude + "\"");
		buffer.append(" gender=\"" + gender + "\"");
		buffer.append(" nickname=\"" + nickname + "\"");
		buffer.append(" latitude=\"" + latitude + "\" jid=\"" + jid
				+ "\"></item>");

		return buffer.toString();
	}

}
