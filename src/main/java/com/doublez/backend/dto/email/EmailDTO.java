package com.doublez.backend.dto.email;

import java.util.ArrayList;
import java.util.List;

public class EmailDTO {
	private String to;
	private String subject;
	private String body;
	private boolean isHtml = false;
	private List<String> cc = new ArrayList<>();
	private List<String> bcc = new ArrayList<>();

	// Constructors
	public EmailDTO() {
	}

	public EmailDTO(String to, String subject, String body) {
		this.to = to;
		this.subject = subject;
		this.body = body;
	}

	public EmailDTO(String to, String subject, String body, boolean isHtml) {
		this(to, subject, body);
		this.isHtml = isHtml;
	}

	// Getters and Setters
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean html) {
		isHtml = html;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}
}
