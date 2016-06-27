package com.stranger.client.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.packet.IQ;

import com.stranger.client.util.LocationItem;

public class LocationIQ extends IQ {
	public static final String ELEMENT = "query";
	public static final String NAMESPACE = "urn:xmpp:rayo:lbsservice"; // ָ�������ռ�
	// ����˽�������󽫸��ݴ������ռ����ҵ�����ദ����Ӧ����
	// ������B/S����Ӧ��ʽ�������
	// [java] view
	// plaincopyprint?
	private final List<LocationItem> items;
	private String node;

	@SuppressWarnings("unchecked")
	public LocationIQ() {
		this.items = new CopyOnWriteArrayList();
	}

	public void addItem(LocationItem item) {
		synchronized (this.items) {
			this.items.add(item);
		}
	}

	public Iterator<LocationItem> getItems() {
		synchronized (this.items) {
			return Collections.unmodifiableList(this.items).iterator();
		}
	}

	public int size() {
		return items.size();
	}

	public String getNode() {
		return this.node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	// /�ָ����ݵ�XML��ʽ
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();

		buf.append("<query xmlns=\"urn:xmpp:rayo:lbsservice\"");

		if (getNode() != null) {
			buf.append(" node=\"");
			buf.append(getNode());
			buf.append("\"");
		}

		buf.append(">");

		synchronized (this.items) {
			for (LocationItem item : this.items) {
				buf.append(item.toXML());
			}
		}

		buf.append("</query>");

		return buf.toString();
	}
}
