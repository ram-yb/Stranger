package com.stranger.client.util;

import java.io.IOException;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LocationIQProvider implements IQProvider {

	private static final String PREFERRED_ENCODING = "UTF-8";

	/*** 通过这个方法我们可以获取从服务器传来iq **/

	private String namespace, element;

	public LocationIQProvider(String element, String namespace) {
		this.element = element;
		this.namespace = namespace;
	}

	public LocationIQ parseIQ(XmlPullParser parser) throws Exception {

		LocationIQ iq = new LocationIQ();
		String xml = iq.getChildElementXML();

		final StringBuilder sb = new StringBuilder();

		try {

			int event = parser.getEventType();

			// get the content

			while (true) {

				switch (event) {

				case XmlPullParser.TEXT:

					// We must re-escape the xml so that the DOM won't throw an
					// exception

					sb.append(StringUtils.escapeForXML(parser.getText()));

					break;

				case XmlPullParser.START_TAG:

					String name1 = parser.getName();
					sb.append('<').append(name1).append('>');

					LocationItem item = new LocationItem();

					if (name1.equals("item")) {
						item.setUsername(parser.getAttributeValue(0));
						item.setLongitude(parser.getAttributeValue(1));
						item.setLatitude(parser.getAttributeValue(2));
						item.setUpdatetime(parser.getAttributeValue(3));
						item.setGender(parser.getAttributeValue(4));
						item.setNickname(parser.getAttributeValue(5));
						item.setJid(parser.getAttributeValue(6));
						iq.addItem(item);
					}

					break;

				case XmlPullParser.END_TAG:

					sb.append("</").append(parser.getName()).append('>');
					break;

				default:

				}

				if (event == XmlPullParser.END_TAG
						&& element.equals(parser.getName()))
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

		xml = xmlText;
		return iq;

	}

}
