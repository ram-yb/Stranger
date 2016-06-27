package com.stranger.client.util;

import java.util.Date;
import java.util.Map;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.Message.Subject;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;

public class FileMessage extends Message {

	public static final String REQUEST = "request";
	public static final String REJECT = "reject";
	public static final String ACCEPT = "accept";
	public static final String SENDING = "sending";
	public static final String COMPLETE = "complete";
	public static final String FAIL = "fail";
	public static final String ERROR = "error";
	public static final String CANCEL = "cancel";
	private String fileName;
	private String saveFileName;
	private String mime_type;
	private String status;
	private String fileMD5Code;
	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileMD5Code() {
		return fileMD5Code;
	}

	public void setFileMD5Code(String fileMD5Code) {
		this.fileMD5Code = fileMD5Code;
	}

	private long date;

	public FileMessage() {
	}

	public FileMessage(String to) {
		super(to);
	}

	public FileMessage(String to, Type type) {
		super(to, type);
	}

	public FileMessage(Message message) {
		thread = message.getThread();
		type = message.getType();
		language = message.getLanguage();
		this.setSubject(message.getSubject(message.getLanguage()));
		this.setTo(message.getTo());
		this.setFrom(message.getFrom());
		this.setPacketID(message.getPacketID());
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSaveFileName() {
		return saveFileName;
	}

	public void setSaveFileName(String saveFileName) {
		this.saveFileName = saveFileName;
	}

	public String getMime_type() {
		return mime_type;
	}

	public void setMime_type(String mime_type) {
		this.mime_type = mime_type;
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<message");
		if (getXmlns() != null) {
			buf.append(" xmlns=\"").append(getXmlns()).append("\"");
		}
		if (language != null) {
			buf.append(" xml:lang=\"").append(getLanguage()).append("\"");
		}
		if (getPacketID() != null) {
			buf.append(" id=\"").append(getPacketID()).append("\"");
		}
		if (getTo() != null) {
			buf.append(" to=\"").append(StringUtils.escapeForXML(getTo()))
					.append("\"");
		}
		if (getFrom() != null) {
			buf.append(" from=\"").append(StringUtils.escapeForXML(getFrom()))
					.append("\"");
		}
		if (type != Type.normal) {
			buf.append(" type=\"").append(type).append("\"");
		}
		buf.append(">");
		// Add the subject in the default language
		Subject defaultSubject = getMessageSubject(null);
		if (defaultSubject != null) {
			buf.append("<subject>").append(
					StringUtils.escapeForXML(defaultSubject.getSubject()));
			buf.append("</subject>");
		}
		// Add subjects in other languages
		for (Subject s : getSubjects()) {
			buf.append("<subject xml:lang=\"" + s.getLanguage() + "\">");
			buf.append(StringUtils.escapeForXML(s.getSubject()));
			buf.append("</subject>");
		}
		// 添加文件信息
		buf.append("<fileinfo><fileName>" + fileName
				+ "</fileName><saveFileName>" + saveFileName
				+ "</saveFileName><mime_type>" + mime_type
				+ "</mime_type><status>" + status + "</status><date>" + date
				+ "</date><md5>" + fileMD5Code + "</md5><path>" + path
				+ "</path></fileinfo>");

		// Add the body in the default language
		Body defaultBody = getMessageBody(null);
		if (defaultBody != null) {
			buf.append("<body>")
					.append(StringUtils.escapeForXML(defaultBody.getMessage()))
					.append("</body>");
		}
		// Add the bodies in other languages
		for (Body body : getBodies()) {
			// Skip the default language
			if (body.equals(defaultBody))
				continue;
			buf.append("<body xml:lang=\"").append(body.getLanguage())
					.append("\">");
			buf.append(StringUtils.escapeForXML(body.getMessage()));
			buf.append("</body>");
		}
		if (getThread() != null) {
			buf.append("<thread>").append(getThread()).append("</thread>");
		}
		// Append the error subpacket if the message type is an error.
		if (getType() == Type.error) {
			XMPPError error = getError();
			if (error != null) {
				buf.append(error.toXML());
			}
		}
		// Add packet extensions, if any are defined.
		buf.append(getExtensionsXML());
		buf.append("</message>");
		return buf.toString();
	}

}
