package com.huinan.server.service.data;

import java.io.Serializable;

/**
 *
 * renchao
 */
public class Card implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1576136289744526484L;
	private int num;
	/**
	 * 牌的位置
	 */
	private int seat;
	/**
	 * 是否翻开
	 */
	private boolean open = true;
	/**
	 * 是否是出牌(手里打出去的,不然就是翻开的)
	 */
	private boolean chu;
	/**
	 * 是否是庄家打出的第一张牌
	 */
	private boolean firstCard;
	/**
	 * 是否是扯了摸起来的牌
	 */
	private boolean cheMo;

	private boolean feiTian25;

	public Card(int num) {
		this.num = num;
	}

	public Card(int num, int seat, boolean open, boolean chu, boolean cheMo,
			boolean firstCard) {
		this.num = num;
		this.seat = seat;
		this.open = open;
		this.chu = chu;
		this.cheMo = cheMo;
		this.firstCard = firstCard;
	}

	public int getCardValue() {
		return num / 10 + num % 10;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public boolean isFirstCard() {
		return firstCard;
	}

	public void setFirstCard(boolean firstCard) {
		this.firstCard = firstCard;
	}

	public boolean isCheMo() {
		return cheMo;
	}

	public void setCheMo(boolean cheMo) {
		this.cheMo = cheMo;
	}

	public boolean isChu() {
		return chu;
	}

	public void setChu(boolean chu) {
		this.chu = chu;
	}

	public boolean isFeiTian25() {
		return feiTian25;
	}

	public void setFeiTian25(boolean feiTian25) {
		this.feiTian25 = feiTian25;
	}

}
