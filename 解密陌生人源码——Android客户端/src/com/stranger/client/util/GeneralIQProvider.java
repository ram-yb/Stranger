package com.stranger.client.util;

import java.io.IOException;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.R.integer;

//只能解析到孩子节点的一级子节点
public class GeneralIQProvider implements IQProvider {

	private static final String PREFERRED_ENCODING = "UTF-8";

	/*** 通过这个方法我们可以获取从服务器传来iq **/

	private String namespace, elementName;

	public GeneralIQProvider(String elementName, String namespace) {
		this.elementName = elementName;
		this.namespace = namespace;
	}

	public GeneralIQ parseIQ(XmlPullParser parser) throws Exception {

		System.out.println("-->>>GeneralIQProvider");
		GeneralIQ iq = new GeneralIQ();
		String xml = iq.getChildElementXML();
		DocumentFactory factory = new DocumentFactory();
		Element currentelement = null;
		Element childElement = null;

		final StringBuilder sb = new StringBuilder();

		try {

			int event = parser.getEventType();

			// get the content

			while (true) {

				switch (event) {

				case XmlPullParser.TEXT:

					// We must re-escape the xml so that the DOM won't throw an
					// exception
					currentelement.addText(parser.getText());
					sb.append(StringUtils.escapeForXML(parser.getText()));

					break;

				case XmlPullParser.START_TAG:

					String name1 = parser.getName();
					if ("iq".equals(name1)) {
						int count = parser.getAttributeCount();
						for (int i = 0; i < count; i++) {
							String name = parser.getAttributeName(i);
							String value = parser.getAttributeValue(i);
							if ("to".equals(name))
								iq.setTo(value);
							else if ("from".equals(name))
								iq.setFrom(value);
							else if ("type".equals(name))
								iq.setType(IQ.Type.fromString(value));
							else if ("id".equals(name))
								iq.setPacketID(value);
							// else if ("error".equals(name))
							// iq.setError();
						}
						System.out.println("START_TAG-->>iq");
					} else if (elementName.equals(name1)) {
						currentelement = factory.createElement(name1,
								parser.getNamespace());
						int count = parser.getAttributeCount();
						for (int i = 0; i < count; i++) {
							currentelement.addAttribute(
									parser.getAttributeName(i),
									parser.getAttributeValue(i));
						}
						childElement = currentelement;
						System.out.println("START_TAG-->>" + name1);

					} else {
						currentelement = factory.createElement(name1,
								parser.getNamespace());
						int count = parser.getAttributeCount();
						for (int i = 0; i < count; i++) {
							currentelement.addAttribute(
									parser.getAttributeName(i),
									parser.getAttributeValue(i));
						}
						System.out.println("START_TAG-->>" + name1);
					}
					break;

				case XmlPullParser.END_TAG:
					if (!elementName.equals(parser.getName()))
						childElement.add(currentelement);
					currentelement = null;
					sb.append("</").append(parser.getName()).append('>');
					break;

				default:

				}

				if (event == XmlPullParser.END_TAG
						&& elementName.equals(parser.getName()))
					break;

				event = parser.next();

			}

		}

		catch (XmlPullParserException e) {

			e.printStackTrace();

		}

		catch (IOException e) {

			e.printStackTrace();

		}

		String xmlText = sb.toString();

		iq.setChildElement(childElement);
		iq.setChildElementName(elementName);
		System.out.println("iq.XML-->>" + iq.getChildElementXML());
		return iq;

	}

}
