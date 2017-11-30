package com.huinan.server.service.data.club;

public class ClubRoom {

	private int clubId;
	private int roomId;
	private byte[] totalData;
	private int status;

	public ClubRoom(int clubId, int roomId) {
		this.clubId = clubId;
		this.roomId = roomId;
	}

	public int getClubId() {
		return clubId;
	}

	public void setClubId(int clubId) {
		this.clubId = clubId;
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
