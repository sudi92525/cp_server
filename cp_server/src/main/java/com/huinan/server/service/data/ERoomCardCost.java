package com.huinan.server.service.data;

/**
 *
 * renchao
 */
public enum ERoomCardCost {
	four(8, 4, 3), eight(12, 4, 4), sixteen(16, 4, 5), three_four(8, 3, 2), three_eight(
			12, 3, 3), three_sixteen(16, 3, 4);

	public int round;
	public int playerNum;
	public int num;

	private ERoomCardCost(int round, int playerNum, int num) {
		this.round = round;
		this.playerNum = playerNum;
		this.num = num;
	}

	public static int getRoomCardCost(int round, int playerNum) {
		for (ERoomCardCost be : ERoomCardCost.values()) {
			if (be.getRound() == round && be.getPlayerNum() == playerNum) {
				return be.getNum();
			}
		}
		return 0;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public void setPlayerNum(int playerNum) {
		this.playerNum = playerNum;
	}

}
