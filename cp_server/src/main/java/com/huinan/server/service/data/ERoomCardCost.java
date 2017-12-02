package com.huinan.server.service.data;

/**
 *
 * renchao
 */
public enum ERoomCardCost {
	four(8, 3), eight(12, 4), sixteen(16, 5);

	public int round;
	public int num;

	private ERoomCardCost(int round, int num) {
		this.round = round;
		this.num = num;
	}

	public static int getRoomCardCost(int round) {
		for (ERoomCardCost be : ERoomCardCost.values()) {
			if (be.getRound() == round) {
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

}
