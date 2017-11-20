package com.huinan.server.service.data.club;

import java.util.ArrayList;
import java.util.List;

public class Club {

	/** 俱乐部id */
	private int id;
	/** 群主uid */
	private String creatorId;
	/** 俱乐部名字 */
	private String name;
	/** 俱乐部游戏类型：长牌，麻将 */
	private int gameType;
	/** 俱乐部成员uid */
	private List<String> members = new ArrayList<>();
	/** 申请uid */
	private List<String> applys = new ArrayList<>();
	/** 房间列表 */
	private List<ClubRoom> rooms = new ArrayList<>();
	

	public Club(int id, String creatorId, String name) {
		this.id = id;
		this.creatorId = creatorId;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public List<String> getApplys() {
		return applys;
	}

	public void setApplys(List<String> applys) {
		this.applys = applys;
	}

	public List<ClubRoom> getRooms() {
		return rooms;
	}

	public void setRooms(List<ClubRoom> rooms) {
		this.rooms = rooms;
	}
	
	

}
