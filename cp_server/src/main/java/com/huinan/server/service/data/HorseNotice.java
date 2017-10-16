package com.huinan.server.service.data;

import java.sql.Date;

public class HorseNotice {

	private int id;
	private String message;
	private Date startDate;
	private Date endDate;
	private int interval;

	public HorseNotice(int id, String message, Date startDate, Date endDate,int interval) {
		this.id = id;
		this.message = message;
		this.startDate = startDate;
		this.endDate = endDate;
		this.interval = interval;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

}
