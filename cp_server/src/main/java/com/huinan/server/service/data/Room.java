package com.huinan.server.service.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.math.RandomUtils;

import com.huinan.proto.CpMsgCs.BigResult;
import com.huinan.proto.CpMsgCs.CSNotifyGameOver;
import com.huinan.proto.CpMsgCs.CSNotifyGameStart;
import com.huinan.proto.CpMsgCs.CSRequestCreateTable;
import com.huinan.proto.CpMsgCs.DissolveList;
import com.huinan.proto.CpMsgCs.ENRoomType;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class Room implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8558031302751533781L;
	/**
	 * 房间编号
	 */
	private int tid;
	/**
	 * 局数（第几局）
	 */
	private int round = 1;
	/**
	 * 房间信息
	 */
	private CSRequestCreateTable roomTable;
	/**
	 * 房间用户信息（包括昵称、头像、座位信息等）
	 */
	private CSNotifyGameOver userInfo;

	private CSNotifyGameStart gameStart;
	/**
	 * 各用户大结算数据缓存
	 */
	private List<BigResult> bigResults;
	/**
	 * 各用户解散游戏数据缓存
	 */
	private List<DissolveList> disList;
	/**
	 * 游戏解散发起人uuid
	 */
	private String launch_uuid;
	/** 开始解散的时间，解散失败重置为0 */
	private long startDissolveTime;
	/** 上次进入房间的时间 */
	private long lastEnterTime;
	// -------------------------创房选项----------------------------------
	/** GY:是否有包翻的功能 */
	private boolean baoFan;
	/** GY:是否吃红只能打黑 */
	private boolean chiHongDaHei = true;
	/** GY:小家全黑两番 */
	private boolean blackTwoFan;
	/** 房间类型:广元,南充 */
	private int roomType = ENRoomType.EN_ROOM_TYPE_GY_VALUE;
	/** 是否有漂的功能 */
	private boolean piao;
	/** 是否丁斧两边甩 */
	private boolean dingFuColor = true;
	/** 算分类型:加番,乘番 */
	private boolean addFan = false;
	/** 是否五黑一红可以割 */
	private boolean wuHeiYiHong;
	/** 是否有追牌的功能:只针对丁斧,天地 */
	private boolean cheZhui;
	/** 吊追 */
	private boolean diaoZhui;
	/** 是否有招割的功能:广元不用招 */
	private boolean zhaoHu;
	/** 是否头当 */
	private boolean touDang;
	/** difen */
	private int diFen = 1;
	/** 人数 */
	private int userNum = 4;

	private int huSeat;
	// ---------------西充规则--------------------
	/** 自摸加番 */
	private boolean ziMoJiaFan = true;
	/** 丁丁斧头随便甩 */
	private boolean dingFuShuaiTimes = true;
	/** 是否扯了所有六点加番 */
	private boolean cheAll7Fan;
	/** 是否三四算番 */
	private boolean jiaFan34;
	/** 是否18烂 */
	private boolean lan18;
	/** 是否可以弯叫 :默认为可以弯叫 */
	private boolean canNotWanJiao;
	/** 是否三砍黑加翻 */
	private boolean sanKanHeiIsFan = true;
	// -------------------------------苍溪版本-----------------------------------
	/** 是否小家有斧头的全红全黑算番 */
	private boolean fanFiveHave56;

	// -------------------------------南充版本-----------------------------------
	private List<Integer> fanPais = new ArrayList<>();
	private List<Integer> allPais = new ArrayList<>();
	/** 庄家 */
	private int zhuangSeat = 1;
	/** 玩家<座位,User> */
	private Map<Integer, User> users = new ConcurrentHashMap<>();
	/** 剩余牌 */
	private List<Integer> resetCards = new ArrayList<>();
	/** 同意解散的玩家 */
	private Map<String, Boolean> agreeDissolveUsers = new ConcurrentHashMap<>();
	private boolean start;
	// ----------执行了过后清除以下数据-----------------
	/** 当前打出的牌 */
	private Card currentCard;
	/** 当前能操作的玩家,按优先级排序 */
	private List<Integer> canActionSeat = new ArrayList<>();
	/** 当前玩家操作记录,执行优先级最高的或者选择中优先级最高的<seat,type> */
	private Map<Integer, Integer> actionRecord = new ConcurrentHashMap<>();
	/** 当前能胡的玩家 */
	private List<Integer> canHuSeat = new ArrayList<>();
	/** 可胡玩家的选择<seat,type> */
	private Map<Integer, Boolean> huChoices = new ConcurrentHashMap<>();
	/** 当前能吃的玩家 */
	private List<Integer> canChiSeat = new ArrayList<>();
	/** 当前能吃的玩家 */
	private List<Integer> canChiSeatTemp = new ArrayList<>();
	/** 可胡玩家的选择<seat,type> */
	private Map<Integer, Boolean> chiChoices = new ConcurrentHashMap<>();
	/** 当前能Che的玩家 */
	private int canCheSeat;
	/** 可扯的是否扯 */
	private boolean che = false;
	/** 可扯的是否选择 */
	private boolean choiceChe = false;
	/** 当前胡的玩家 */
	private List<Integer> currentHuSeat = new ArrayList<>();
	/** 上次出牌玩家(就是打出currentMj的人) */
	private int lastOutSeatIndex;
	/** 操作玩家座位 */
	private int actionSeat;
	/** 是否开始出牌 */
	private boolean startChu;
	/** 包子玩家座位 */
	private int baoZiSeat;
	/** 当家座位 */
	private int dangSeat;
	/** 房间步骤（用于重连） */
	private boolean stepIsPlay;
	/** 是否是庄家打出的第一张 */
	private boolean firstCard;
	/** 上一把胡的位置 */
	private int lastHuSeat;
	/** 开始叫牌位置 */
	private int jiaoPaiSeat;
	/** 叫庄的牌 */
	private int choiceZhuangCard;
	/** 上一次出牌作为 */
	private int lastChuSeat;
	/** 西充烂18座位 */
	private int lan18Seat;
	/** 是否是自动扯7:所有胡的人点了过判断是否扯7 */
	private int che7Seat;

	public Room(int tid, int roomType, boolean dingFuColor,
			boolean dingFu34IsFan, boolean jiaFan34) {
		this.tid = tid;
		this.roomType = roomType;
		this.dingFuColor = dingFuColor;
		// this.dingFu34IsFan = dingFu34IsFan;
		this.jiaFan34 = jiaFan34;

		if (this.roomType == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
		} else {
			fanPais.add(11);
			fanPais.add(13);
			fanPais.add(14);
			fanPais.add(44);
			fanPais.add(66);
		}
		if (this.jiaFan34) {
			fanPais.add(34);
		} else if (this.roomType == ENRoomType.EN_ROOM_TYPE_CX_VALUE) {
			fanPais.add(34);
		}
		if (roomType == ENRoomType.EN_ROOM_TYPE_NC_VALUE) {
			dingFuShuaiTimes = false;
			ziMoJiaFan = true;
			wuHeiYiHong = false;
		} else if (roomType == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {// 西充
			// 西充,可以甩多次
			dingFuShuaiTimes = true;
			ziMoJiaFan = true;
			wuHeiYiHong = false;
		} else if (roomType == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
			dingFuShuaiTimes = false;
			ziMoJiaFan = true;
			wuHeiYiHong = false;
		}

		for (int i = 1; i <= 6; i++) {
			for (int j = i; j <= 6; j++) {
				int card = i * 10 + j;
				allPais.add(card);
			}
		}

		this.lastEnterTime = System.currentTimeMillis();
	}

	public int getFirstCard() {
		int index = RandomUtils.nextInt(this.resetCards.size());
		Integer card = this.resetCards.get(index);// index
		this.resetCards.remove(index);// index
		return card;
	}

	public int getSiPai(int card) {
		for (Integer integer : resetCards) {
			if (integer == card) {
				resetCards.remove(integer);
				return card;
			}
		}
		return getFirstCard();
	}

	/**
	 * 是否能马上胡,还是前面胡的人表态
	 * 
	 * @return
	 */
	public boolean canHuNow(User user) {
		if (canHuSeat.size() == 1) {
			return true;
		} else {
			Room room = RoomManager.getInstance().getRoom(user.getRoomId());
			int firstSeat = 0;
			int currentSeat = currentCard.getSeat();
			if (currentCard.isChu()) {// 手里打出的从下一家开始判断
				firstSeat = 1;
				currentSeat = RoomManager.getNextSeat(room,
						currentCard.getSeat());
			}
			for (int i = firstSeat; i < getUserNum(); i++) {
				if (currentSeat == user.getSeatIndex()) {
					return true;// 判断我自己,可以胡
				}
				// if (canHuSeat.contains(currentSeat)
				// && (huChoices.get(currentSeat) == null
				// && currentSeat != user.getSeatIndex() || huChoices
				// .get(currentSeat))) {
				// return false;// user前面有人胡,且没有选
				// }
				if (canHuSeat.contains(currentSeat)) {
					if (huChoices.get(currentSeat) == null) {
						return false;// user前面有人胡,且没有选
					}
					if (huChoices.get(currentSeat)) {
						return false;// user前面有人点了胡
					}
				}
				currentSeat = RoomManager.getNextSeat(room, currentSeat);
			}
		}
		return true;
	}

	/**
	 * 是否能马上扯,还是等胡的人表态
	 * 
	 * @return
	 */
	public boolean canCheNow() {
		if (canHuSeat.isEmpty()) {
			return true;
		} else {
			if (huChoices.size() == canHuSeat.size()) {
				Iterator<Integer> iterator = huChoices.keySet().iterator();
				while (iterator.hasNext()) {
					Integer integer = (Integer) iterator.next();
					boolean hu = huChoices.get(integer);
					if (hu) {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否能马上吃,还是等胡喝扯的人表态
	 * 
	 * @return
	 */
	public boolean canChiNow(User user) {
		if (!canHuSeat.isEmpty()) {
			if (huChoices.size() == canHuSeat.size()) {
				Iterator<Integer> iterator = huChoices.keySet().iterator();
				while (iterator.hasNext()) {
					Integer integer = (Integer) iterator.next();
					boolean hu = huChoices.get(integer);
					if (hu) {// 要胡
						return false;
					}
				}
			} else {// 胡的人还没选完
				return false;
			}
		} else if (canCheSeat != 0 && (!choiceChe || che)) {// 要扯
			return false;
		} else if (canChiSeat.size() > 1
				&& user.getSeatIndex() != currentCard.getSeat()
				&& (!chiChoices.containsKey(currentCard.getSeat()) || chiChoices
						.get(currentCard.getSeat()))) {// 出牌的要要吃,且还没选吃
			return false;
		}
		return true;
	}

	// TODO 新增属性时记得结算清理
	public void clearRound() {
		this.startChu = false;
		this.dangSeat = 0;
		this.currentCard = null;
		this.stepIsPlay = false;
		this.firstCard = true;
		this.lastChuSeat = 0;
		this.huSeat = 0;
		this.lan18Seat = 0;
		clearCurrentInfo();
		for (User user : users.values()) {
			user.clearRoundData();
		}
	}

	public void clearCurrentInfo() {
		this.baoZiSeat = 0;
		this.canActionSeat.clear();
		this.canHuSeat.clear();
		this.canCheSeat = 0;
		this.canChiSeat.clear();
		this.canChiSeatTemp.clear();
		this.actionRecord.clear();
		this.currentHuSeat.clear();
		this.lastOutSeatIndex = 0;
		this.huChoices.clear();
		this.chiChoices.clear();
		this.che = false;
		this.choiceChe = false;
		this.currentHuSeat.clear();
		this.che7Seat = 0;
		for (User user : users.values()) {
			user.clearCurrentInfo();
		}
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public Map<Integer, User> getUsers() {
		return users;
	}

	public void setUsers(Map<Integer, User> users) {
		this.users = users;
	}

	public Card getCurrentCard() {
		return currentCard;
	}

	public void setCurrentCard(Card currentCard) {
		this.currentCard = currentCard;
	}

	public List<Integer> getCanActionSeat() {
		return canActionSeat;
	}

	public void setCanActionSeat(List<Integer> canActionSeat) {
		this.canActionSeat = canActionSeat;
	}

	/** 能胡的玩家,选过后从中删除 */
	public List<Integer> getCanHuSeat() {
		return canHuSeat;
	}

	public void setCanHuSeat(List<Integer> canHuSeat) {
		this.canHuSeat = canHuSeat;
	}

	public List<Integer> getCurrentHuSeat() {
		return currentHuSeat;
	}

	public void setCurrentHuSeat(List<Integer> currentHuSeat) {
		this.currentHuSeat = currentHuSeat;
	}

	public Map<Integer, Integer> getActionRecord() {
		return actionRecord;
	}

	public void setActionRecord(Map<Integer, Integer> actionRecord) {
		this.actionRecord = actionRecord;
	}

	public int getLastOutSeatIndex() {
		return lastOutSeatIndex;
	}

	public void setLastOutSeatIndex(int lastOutSeatIndex) {
		this.lastOutSeatIndex = lastOutSeatIndex;
	}

	public int getActionSeat() {
		return actionSeat;
	}

	public void setActionSeat(int actionSeat) {
		this.actionSeat = actionSeat;
	}

	public Map<Integer, Boolean> getHuChoices() {
		return huChoices;
	}

	public void setHuChoices(Map<Integer, Boolean> huChoices) {
		this.huChoices = huChoices;
	}

	public int getCanCheSeat() {
		return canCheSeat;
	}

	public void setCanCheSeat(int canCheSeat) {
		this.canCheSeat = canCheSeat;
	}

	public boolean isChe() {
		return che;
	}

	public void setChe(boolean che) {
		this.che = che;
	}

	public List<Integer> getCanChiSeat() {
		return canChiSeat;
	}

	public void setCanChiSeat(List<Integer> canChiSeat) {
		this.canChiSeat = canChiSeat;
	}

	public Map<Integer, Boolean> getChiChoices() {
		return chiChoices;
	}

	public void setChiChoices(Map<Integer, Boolean> chiChoices) {
		this.chiChoices = chiChoices;
	}

	public List<Integer> getResetCards() {
		return resetCards;
	}

	public void setResetCards(List<Integer> resetCards) {
		this.resetCards = resetCards;
	}

	public int getZhuangSeat() {
		return zhuangSeat;
	}

	public void setZhuangSeat(int zhuangSeat) {
		this.zhuangSeat = zhuangSeat;
	}

	public void incrRound() {
		this.round++;
	}

	public boolean isAddFan() {
		return addFan;
	}

	public void setAddFan(boolean addFan) {
		this.addFan = addFan;
	}

	public boolean isDingFuColor() {
		return dingFuColor;
	}

	public void setDingFuColor(boolean dingFuColor) {
		this.dingFuColor = dingFuColor;
	}

	public List<Integer> getFanPais() {
		return fanPais;
	}

	public void setFanPais(List<Integer> fanPais) {
		this.fanPais = fanPais;
	}

	public List<Integer> getAllPais() {
		return allPais;
	}

	public void setAllPais(List<Integer> allPais) {
		this.allPais = allPais;
	}

	public boolean isChoiceChe() {
		return choiceChe;
	}

	public void setChoiceChe(boolean choiceChe) {
		this.choiceChe = choiceChe;
	}

	public boolean isStartChu() {
		return startChu;
	}

	public void setStartChu(boolean startChu) {
		this.startChu = startChu;
	}

	public Map<String, Boolean> getAgreeDissolveUsers() {
		return agreeDissolveUsers;
	}

	public void setAgreeDissolveUsers(Map<String, Boolean> agreeDissolveUsers) {
		this.agreeDissolveUsers = agreeDissolveUsers;
	}

	/**
	 * 开始漂,或者开始发牌
	 * 
	 * @return
	 */
	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public int getBaoZiSeat() {
		return baoZiSeat;
	}

	public void setBaoZiSeat(int baoZiSeat) {
		this.baoZiSeat = baoZiSeat;
	}

	public int getDangSeat() {
		return dangSeat;
	}

	public void setDangSeat(int dangSeat) {
		this.dangSeat = dangSeat;
	}

	public boolean isPiao() {
		return piao;
	}

	public void setPiao(boolean piao) {
		this.piao = piao;
	}

	public boolean isStepIsPlay() {
		return stepIsPlay;
	}

	public void setStepIsPlay(boolean stepIsPlay) {
		this.stepIsPlay = stepIsPlay;
	}

	public boolean isFirstCard() {
		return firstCard;
	}

	public void setFirstCard(boolean firstCard) {
		this.firstCard = firstCard;
	}

	public boolean isBaoFan() {
		return baoFan;
	}

	public void setBaoFan(boolean baoFan) {
		this.baoFan = baoFan;
	}

	public boolean isChiHongDaHei() {
		return chiHongDaHei;
	}

	public void setChiHongDaHei(boolean chiHongDaHei) {
		this.chiHongDaHei = chiHongDaHei;
	}

	public boolean isBlackTwoFan() {
		return blackTwoFan;
	}

	public void setBlackTwoFan(boolean blackTwoFan) {
		this.blackTwoFan = blackTwoFan;
	}

	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public CSRequestCreateTable getRoomTable() {
		return roomTable;
	}

	public void setRoomTable(CSRequestCreateTable roomTable) {
		this.roomTable = roomTable;
	}

	public CSNotifyGameOver getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(CSNotifyGameOver userInfo) {
		this.userInfo = userInfo;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public List<BigResult> getBigResults() {
		return bigResults;
	}

	public void setBigResults(List<BigResult> bigResults) {
		this.bigResults = bigResults;
	}

	public List<DissolveList> getDisList() {
		return disList;
	}

	public void setDisList(List<DissolveList> disList) {
		this.disList = disList;
	}

	public String getLaunch_uuid() {
		return launch_uuid;
	}

	public void setLaunch_uuid(String launch_uuid) {
		this.launch_uuid = launch_uuid;
	}

	public boolean isWuHeiYiHong() {
		return wuHeiYiHong;
	}

	public void setWuHeiYiHong(boolean wuHeiYiHong) {
		this.wuHeiYiHong = wuHeiYiHong;
	}

	public int getLastHuSeat() {
		return lastHuSeat;
	}

	public void setLastHuSeat(int lastHuSeat) {
		this.lastHuSeat = lastHuSeat;
	}

	public int getChoiceZhuangCard() {
		return choiceZhuangCard;
	}

	public void setChoiceZhuangCard(int choiceZhuangCard) {
		this.choiceZhuangCard = choiceZhuangCard;
	}

	public int getJiaoPaiSeat() {
		return jiaoPaiSeat;
	}

	public void setJiaoPaiSeat(int jiaoPaiSeat) {
		this.jiaoPaiSeat = jiaoPaiSeat;
	}

	public boolean isZhaoHu() {
		return zhaoHu;
	}

	public void setZhaoHu(boolean zhaoHu) {
		this.zhaoHu = zhaoHu;
	}

	public boolean isZiMoJiaFan() {
		return ziMoJiaFan;
	}

	public void setZiMoJiaFan(boolean ziMoJiaFan) {
		this.ziMoJiaFan = ziMoJiaFan;
	}

	public boolean isDingFuShuaiTimes() {
		return dingFuShuaiTimes;
	}

	public void setDingFuShuaiTimes(boolean dingFuShuaiTimes) {
		this.dingFuShuaiTimes = dingFuShuaiTimes;
	}

	public boolean isCheZhui() {
		return cheZhui;
	}

	public void setCheZhui(boolean cheZhui) {
		this.cheZhui = cheZhui;
	}

	public boolean isTouDang() {
		return touDang;
	}

	public void setTouDang(boolean touDang) {
		this.touDang = touDang;
	}

	public long getStartDissolveTime() {
		return startDissolveTime;
	}

	public void setStartDissolveTime(long startDissolveTime) {
		this.startDissolveTime = startDissolveTime;
	}

	public boolean isCheAll7Fan() {
		return cheAll7Fan;
	}

	public void setCheAll7Fan(boolean cheAll7Fan) {
		this.cheAll7Fan = cheAll7Fan;
	}

	public boolean isJiaFan34() {
		return jiaFan34;
	}

	public void setJiaFan34(boolean jiaFan34) {
		this.jiaFan34 = jiaFan34;
	}

	public List<Integer> getCanChiSeatTemp() {
		return canChiSeatTemp;
	}

	public void setCanChiSeatTemp(List<Integer> canChiSeatTemp) {
		this.canChiSeatTemp = canChiSeatTemp;
	}

	public CSNotifyGameStart getGameStart() {
		return gameStart;
	}

	public void setGameStart(CSNotifyGameStart gameStart) {
		this.gameStart = gameStart;
	}

	public long getLastEnterTime() {
		return lastEnterTime;
	}

	public void setLastEnterTime(long lastEnterTime) {
		this.lastEnterTime = lastEnterTime;
	}

	public int getLastChuSeat() {
		return lastChuSeat;
	}

	public void setLastChuSeat(int lastChuSeat) {
		this.lastChuSeat = lastChuSeat;
	}

	public boolean isDiaoZhui() {
		return diaoZhui;
	}

	public void setDiaoZhui(boolean diaoZhui) {
		this.diaoZhui = diaoZhui;
	}

	public int getDiFen() {
		return diFen;
	}

	public void setDiFen(int diFen) {
		this.diFen = diFen;
	}

	public int getHuSeat() {
		return huSeat;
	}

	public void setHuSeat(int huSeat) {
		this.huSeat = huSeat;
	}

	public int getLan18Seat() {
		return lan18Seat;
	}

	public void setLan18Seat(int lan18Seat) {
		this.lan18Seat = lan18Seat;
	}

	public boolean isLan18() {
		return lan18;
	}

	public void setLan18(boolean lan18) {
		this.lan18 = lan18;
	}

	public boolean isFanFiveHave56() {
		return fanFiveHave56;
	}

	public void setFanFiveHave56(boolean fanFiveHave56) {
		this.fanFiveHave56 = fanFiveHave56;
	}

	public boolean isCanNotWanJiao() {
		return canNotWanJiao;
	}

	public void setCanNotWanJiao(boolean canNotWanJiao) {
		this.canNotWanJiao = canNotWanJiao;
	}

	public int getUserNum() {
		return userNum;
	}

	public void setUserNum(int userNum) {
		this.userNum = userNum;
	}

	public boolean isSanKanHeiIsFan() {
		return sanKanHeiIsFan;
	}

	public void setSanKanHeiIsFan(boolean sanKanHeiIsFan) {
		this.sanKanHeiIsFan = sanKanHeiIsFan;
	}

	public int getChe7Seat() {
		return che7Seat;
	}

	public void setChe7Seat(int che7Seat) {
		this.che7Seat = che7Seat;
	}

}
