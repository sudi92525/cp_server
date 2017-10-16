package com.huinan.server.service.data;

import com.huinan.proto.CpMsgCs.CSResponsePlayBack;

public class UserScoreRecord {

    private int id;
    private String uid;
    private int roomId;
    private String createTime;
    private int recordType;
    private String userScoreJson;
    private int gameNum;
    private CSResponsePlayBack playBack;
    private int regionType;// 地区
    private int allRoundNum;// 总局数

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public String getUid() {
	return uid;
    }

    public void setUid(String uid) {
	this.uid = uid;
    }

    public int getRoomId() {
	return roomId;
    }

    public void setRoomId(int roomId) {
	this.roomId = roomId;
    }

    public String getCreateTime() {
	return createTime;
    }

    public void setCreateTime(String createTime) {
	this.createTime = createTime;
    }

    public int getRecordType() {
	return recordType;
    }

    public void setRecordType(int recordType) {
	this.recordType = recordType;
    }

    public String getUserScoreJson() {
	return userScoreJson;
    }

    public void setUserScoreJson(String userScoreJson) {
	this.userScoreJson = userScoreJson;
    }

    public int getGameNum() {
	return gameNum;
    }

    public void setGameNum(int gameNum) {
	this.gameNum = gameNum;
    }

    public CSResponsePlayBack getPlayBack() {
	return playBack;
    }

    public void setPlayBack(CSResponsePlayBack playBack) {
	this.playBack = playBack;
    }

    public int getRegionType() {
        return regionType;
    }

    public void setRegionType(int regionType) {
        this.regionType = regionType;
    }

    public int getAllRoundNum() {
        return allRoundNum;
    }

    public void setAllRoundNum(int allRoundNum) {
        this.allRoundNum = allRoundNum;
    }

}
