package com.stranger.client.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.smack.packet.IQ;

public class GeneralIQ extends IQ {

	private String nameSpace; // 指定命名空间
	private Element childElement;
	private String childElementName;// 子节点名称
	// 服务端接收请求后将根据此命名空间调用业务处理类处理并响应请求
	// 可以用B/S求响应方式套用理解
	// [java] view
	// plaincopyprint?
	private String node;

	public GeneralIQ() {
		this.nameSpace = null;
		this.childElementName = null;
		DocumentFactory factory = new DocumentFactory();
		this.childElement = factory.createElement("");
	}

	public GeneralIQ(String childElementName) {
		this.nameSpace = null;
		this.childElementName = childElementName;
		DocumentFactory factory = new DocumentFactory();
		this.childElement = factory.createElement(childElementName);
	}

	public GeneralIQ(String childElementName, String nameSpace) {
		this.nameSpace = nameSpace;
		this.childElementName = childElementName;
		DocumentFactory factory = new DocumentFactory();
		this.childElement = factory.createElement(childElementName);
	}

	public GeneralIQ(Element element) {
		this.nameSpace = element.getNamespace().toString();
		this.childElementName = element.getName();
		childElement = element;
	}

	public void setChildElement(String childElementName, String namespace) {
		this.nameSpace = namespace;
		this.childElementName = childElementName;
		DocumentFactory factory = new DocumentFactory();
		childElement = factory.createElement(childElementName);
	}

	public String getChildElementName() {
		return childElementName;
	}

	public void setChildElementName(String childElementName) {
		this.childElementName = childElementName;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public Element getChildElement() {
		return childElement;
	}

	public void setChildElement(Element childElement) {
		this.childElement = childElement;
	}

	public String getNode() {
		return this.node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	@Override
	public String getXmlns() {
		return this.nameSpace;
	}

	@Override
	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<iq ");
		if (getPacketID() != null)
			sb.append("id=\"" + getPacketID() + "\" ");
		if (getTo() != null)
			sb.append("to=\"" + getTo() + "\" ");
		if (getFrom() != null)
			sb.append("from=\"" + getFrom() + "\" ");
		if (getError() != null)
			sb.append("error=\"" + getError() + "\" ");
		if (getType() != null)
			sb.append("type=\"" + getType() + "\" ");
		sb.append(">");
		sb.append(getChildElementXML());
		sb.append("</iq>");
		return sb.toString();
	}

	// /恢复数据的XML形式 childElement只能有一级子节点
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();

		buf.append("<" + childElementName + " xmlns=\"" + nameSpace + "\"");

		if (getNode() != null) {
			buf.append(" node=\"");
			buf.append(getNode());
			buf.append("\"");
		}

		buf.append(">");

		synchronized (this.childElement) {
			for (Iterator<Element> iterator = childElement.elementIterator(); iterator
					.hasNext();) {
				Element tempeElement = iterator.next();
				buf.append("<" + tempeElement.getName());
				for (Iterator<Attribute> iterator2 = tempeElement
						.attributeIterator(); iterator2.hasNext();) {
					Attribute tempAttribute = iterator2.next();
					buf.append(" " + tempAttribute.getName() + "=\""
							+ tempAttribute.getValue() + "\"");
				}
				buf.append(">" + tempeElement.getTextTrim() + "</"
						+ tempeElement.getName() + ">");
			}
		}

		buf.append("</" + childElementName + ">");

		return buf.toString();
	}

}
