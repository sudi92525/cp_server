package com.huinan.server.service.data.club;

public class ClubRoom {

	private int roomId;
	private byte[] totalData;
	private int status;

	public ClubRoom(int roomId) {
		this.roomId = roomId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public byte[] getTotalData() {
		return totalData;
	}

	public void setTotalData(byte[] totalData) {
		this.totalData = totalData;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	
}
