package com.silence.im.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 联系人模型
 * 
 * @author JerSuen
 */
public class Contact implements Parcelable {
	// index
	public String avatar, account, nickname, index, sort, realname, gender,
			name_by_me, email;

	public static final Creator<Contact> CREATOR = new Creator<Contact>() {

		public Contact createFromParcel(Parcel source) {
			Contact contact = new Contact();
			contact.avatar = source.readString();
			contact.account = source.readString();
			contact.nickname = source.readString();
			contact.index = source.readString();
			contact.sort = source.readString();
			contact.realname = source.readString();
			contact.gender = source.readString();
			contact.name_by_me = source.readString();
			contact.email = source.readString();
			return contact;
		}

		public Contact[] newArray(int size) {
			return new Contact[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(avatar);
		dest.writeString(account);
		dest.writeString(nickname);
		dest.writeString(index);
		dest.writeString(sort);
		dest.writeString(realname);
		dest.writeString(gender);
		dest.writeString(name_by_me);
		dest.writeString(email);
	}
}