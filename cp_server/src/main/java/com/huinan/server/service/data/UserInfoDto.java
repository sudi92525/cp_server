package com.huinan.server.service.data;

public class UserInfoDto {

	/**
	 * 用户唯一编号
	 */
	private String uid;
	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 分数
	 */
	private int userScore;
	/**
	 * 变化分数
	 */
	private int changeScore;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserScore() {
		return userScore;
	}

	public void setUserScore(int userScore) {
		this.userScore = userScore;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getChangeScore() {
		return changeScore;
	}

	public void setChangeScore(int changeScore) {
		this.changeScore = changeScore;
	}
}
