package com.huinan.server.service.data;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huinan.proto.CpMsgCs.CSNotifyGameOver;
import com.huinan.proto.CpMsgCs.CSNotifySeatOperationChoice;
import com.huinan.proto.CpMsgCs.CSResponsePlayBack;
import com.huinan.proto.CpMsgCs.ENActionType;
import com.huinan.proto.CpMsgCs.PBColumnInfo;

/**
 *
 * renchao
 */
public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1700323006684951006L;
	/**
	 * 用户唯一编码
	 */
	private String uuid;
	/**
	 * 用户昵称
	 */
	private String nick;
	/**
	 * 用户头像
	 */
	private String pic_url;
	/**
	 * 性别
	 */
	private int sex;
	/**
	 * 房卡
	 */
	private int roomCardNum;
	/**
	 * 用户授权码
	 */
	private String token;
	/**
	 * 用户连接信息
	 */
	private Socket socket;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getPic_url() {
		return pic_url;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getRoomCardNum() {
		return roomCardNum;
	}

	public synchronized void setRoomCardNum(int roomCardNum) {
		this.roomCardNum = roomCardNum;
	}

	public void decrRoomCardNum(int costNum) {
		this.roomCardNum -= costNum;
		if (this.roomCardNum < 0) {
			this.roomCardNum = 0;
		}
	}

	// -------------------------------南充版本-----------------------------------
	/** 用户授权码 */
	private String ip;
	/** 是否在线 */
	private boolean online = true;
	/** 房间id */
	private int roomId;
	/** 座位(-1表示不在房间) */
	private int seatIndex = -1;
	/** 是否准备 */
	private boolean ready;
	/** 是否同意解散房间 */
	private boolean agreeDissolve;
	/** 本次解散是否表态 */
	private boolean choiceDissolve;
	/** 货币(积分) */
	private int currency;
	/** 积分变化 */
	private int changeCurrency;
	/** 赢的次数 */
	private int huTimes;
	/** 点炮的次数 */
	private int dianPaoTimes;
	/** 自摸的次数 */
	private int ziMoTimes;
	/** 三番以上的次数 */
	private int sanFanTimes;
	/** 是否胡了 */
	private boolean hu;
	/** 点炮玩家坐标 (是自己就是自摸) */
	private int fireIndex;
	/** 是否是出第一张牌 */
	private boolean outFirstMj = true;
	/** 胡的牌 */
	private Card huCard;
	/** 打出的牌可执行哪些操作 */
	private List<ENActionType> actions = new ArrayList<>();
	/** 是否是五张 */
	private boolean five;
	/** 是否是尾家 */
	private boolean last;
	/** 初始化手牌数量 */
	private int initHoldSize;
	/** 当前坨数 */
	private int tuo;
	/** 胡的坨数 */
	private int huTuoNum;
	/** 胡的番数 */
	private int huFanNum;
	/** 用哪张牌吃 */
	private List<Integer> choiceChiCards = new ArrayList<>();
	/** 底分 */
	private int initScore = 1;
	/** 是否招扯 */
	private boolean zhaoChe;
	/** 是否招吃:南充西充招吃了不能割 */
	private boolean zhaoChiNoGe;
	/** 五张起手可以偷的牌 */
	private int touCard;
	/** 是否包子 */
	private boolean baoZi;
	private int huType;
	/** 是否漂 */
	private boolean piao;
	/** 是否选择漂 */
	private boolean choicePiao;
	/** 是否选择当 */
	private boolean choiceDang;
	/** 能够天胡 */
	private boolean canTianHu;
	/** 重连，是否进入房间 */
	private boolean enterRoom = true;
	/** GY:四根数量, */
	private int sigenNum;
	/** GY:包翻信息<seat,几个包翻>, */
	private Map<Integer, Integer> baoFans = new HashMap<>();
	/** GY:是否胡成四根,结算加一番 */
	private boolean huSiGen;
	/** 本次出牌是否是追,是-出完后需要亮起其他牌 */
	private boolean thisChuIsZhui;
	// ------------玩家牌信息------------------------
	/**
	 * 手牌:手里的牌
	 */
	private List<Integer> hold = new ArrayList<>();
	/**
	 * 扣得牌
	 */
	private List<Integer> kou = new ArrayList<>();
	/**
	 * 扯吃牌堆<PBColumnInfo>
	 */
	private List<PBColumnInfo> open = new ArrayList<>();
	/**
	 * 不能碰的牌
	 */
	public List<Integer> noCheCards = new ArrayList<>();
	/**
	 * 不能吃的牌
	 */
	public List<Integer> noChiCards = new ArrayList<>();
	/**
	 * 目标牌,手里不能吃的牌<目标牌,手里哪些牌不能吃他>
	 */
	public Map<Integer, List<Integer>> cardNoChiList = new HashMap<>();
	/**
	 * 该玩家所有吃过的牌
	 */
	public List<Integer> chiCards = new ArrayList<>();
	/**
	 * 不能胡的牌
	 */
	public List<Integer> noHuCards = new ArrayList<>();
	/**
	 * 该玩家真正出过的牌，不包含翻开的牌
	 */
	public List<Integer> chuCards = new ArrayList<>();
	/**
	 * 该玩家右下角的出牌列表
	 */
	public List<Integer> chuListCards = new ArrayList<>();
	/**
	 * 该玩家经过手的牌：出的牌和翻开的牌
	 */
	public List<Integer> guoShouCards = new ArrayList<>();
	public List<Integer> noChuCards = new ArrayList<>();
	public List<Integer> noChuZhuiCards = new ArrayList<>();
	/**
	 * 一对七不能拆开（吃）
	 */
	public List<Integer> double7s = new ArrayList<>();
	/**
	 * 南充吊追单独一对天地，，不能拆开（吃）
	 */
	public List<Integer> doubleZhuiCards = new ArrayList<>();
	/**
	 * 自己出的或发的牌被下家吃了的牌集合
	 */
	public List<Integer> nextChiCards = new ArrayList<>();
	/**
	 * 招吃列表
	 */
	public List<Integer> zhaoChiCards = new ArrayList<>();
	/**
	 * 手牌中,能吃用来吃的牌:不吃时删除对应点数的所有牌,招时加入
	 */
	public List<Integer> canChiHoldCards = new ArrayList<>();

	/** 是否过了飞天25 */
	private boolean feiTian25Pass;
	/** 是否拿过牌：南充小家没摸过牌可以割任何七点 */
	private boolean moPai;
	/** 是否弯叫 */
	private boolean wanJiao;
	/** 下叫点数 */
	private int numJiao;
	/**
	 * 回访数据
	 */
	private CSResponsePlayBack playBack;
	// ----------------重连------------------------
	private CSNotifySeatOperationChoice choice;
	private CSNotifyGameOver gameOver;

	public User(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * 清理所有打牌过程数据
	 */
	public void clear() {
		this.roomId = 0;
		this.seatIndex = -1;
		this.currency = 0;
		this.piao = false;
		this.choicePiao = false;
		this.gameOver = null;
		this.huTimes = 0;
		this.dianPaoTimes = 0;
		this.ziMoTimes = 0;
		this.sanFanTimes = 0;
		clearRoundData();
	}

	/**
	 * 清理上一局数据
	 */
	public void clearRoundData() {
		this.ready = false;
		this.agreeDissolve = false;
		this.changeCurrency = 0;
		this.hold.clear();
		this.kou.clear();
		this.canChiHoldCards.clear();
		this.open.clear();
		this.noCheCards.clear();
		this.noChiCards.clear();
		this.noChuCards.clear();
		this.noChuZhuiCards.clear();
		this.double7s.clear();
		this.doubleZhuiCards.clear();
		this.noHuCards.clear();
		this.chiCards.clear();
		this.chuCards.clear();
		this.chuListCards.clear();
		this.nextChiCards.clear();
		this.fireIndex = 0;
		this.hu = false;
		this.outFirstMj = true;
		this.five = false;
		this.last = false;
		this.huCard = null;
		this.huFanNum = 0;
		this.huTuoNum = 0;
		this.fireIndex = 0;
		this.tuo = 0;
		this.touCard = 0;
		this.baoZi = false;
		this.huType = 0;
		this.choiceDang = false;
		this.canTianHu = false;
		this.initHoldSize = 0;
		this.baoFans.clear();
		this.huSiGen = false;
		this.guoShouCards.clear();
		this.playBack = null;
		this.zhaoChiNoGe = false;
		this.thisChuIsZhui = false;
		this.feiTian25Pass = false;
		this.moPai = false;
		this.wanJiao = false;
		this.numJiao = 0;
		clearCurrentInfo();
	}

	/** 清除每张牌产生的数据 */
	public void clearCurrentInfo() {
		this.actions.clear();
		this.choiceChiCards.clear();
		this.zhaoChe = false;
		this.zhaoChiCards.clear();
		this.choice = null;
		// TODO
	}

	public List<Integer> getOpenList() {
		List<Integer> openList = new ArrayList<>();
		for (PBColumnInfo info : open) {
			openList.addAll(info.getCardsList());
		}
		return openList;
	}

	public void incrHuTimes() {
		this.huTimes++;
	}

	public void incrDianPaoTimes() {
		this.dianPaoTimes++;
	}

	public void incrZiMoTimes() {
		this.ziMoTimes++;
	}

	public void incrSanFanTimes() {
		this.sanFanTimes++;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getSeatIndex() {
		return seatIndex;
	}

	public void setSeatIndex(int seatIndex) {
		this.seatIndex = seatIndex;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isAgreeDissolve() {
		return agreeDissolve;
	}

	public void setAgreeDissolve(boolean agreeDissolve) {
		this.agreeDissolve = agreeDissolve;
	}

	public boolean isChoiceDissolve() {
		return choiceDissolve;
	}

	public void setChoiceDissolve(boolean choiceDissolve) {
		this.choiceDissolve = choiceDissolve;
	}

	public int getCurrency() {
		return currency;
	}

	public void setCurrency(int currency) {
		this.currency = currency;
	}

	public int getChangeCurrency() {
		return changeCurrency;
	}

	public void setChangeCurrency(int changeCurrency) {
		this.changeCurrency = changeCurrency;
	}

	public int getHuTimes() {
		return huTimes;
	}

	public void setHuTimes(int huTimes) {
		this.huTimes = huTimes;
	}

	public int getDianPaoTimes() {
		return dianPaoTimes;
	}

	public void setDianPaoTimes(int dianPaoTimes) {
		this.dianPaoTimes = dianPaoTimes;
	}

	public int getZiMoTimes() {
		return ziMoTimes;
	}

	public void setZiMoTimes(int ziMoTimes) {
		this.ziMoTimes = ziMoTimes;
	}

	public int getSanFanTimes() {
		return sanFanTimes;
	}

	public void setSanFanTimes(int sanFanTimes) {
		this.sanFanTimes = sanFanTimes;
	}

	public boolean isHu() {
		return hu;
	}

	public void setHu(boolean hu) {
		this.hu = hu;
	}

	public int getFireIndex() {
		return fireIndex;
	}

	public void setFireIndex(int fireIndex) {
		this.fireIndex = fireIndex;
	}

	public boolean isOutFirstMj() {
		return outFirstMj;
	}

	public void setOutFirstMj(boolean outFirstMj) {
		this.outFirstMj = outFirstMj;
	}

	public Card getHuCard() {
		return huCard;
	}

	public void setHuCard(Card huCard) {
		this.huCard = huCard;
	}

	public List<ENActionType> getActions() {
		return actions;
	}

	public void setActions(List<ENActionType> actions) {
		this.actions = actions;
	}

	public List<Integer> getHold() {
		return hold;
	}

	public void setHold(List<Integer> hold) {
		this.hold = hold;
	}

	public List<PBColumnInfo> getOpen() {
		return open;
	}

	public void setOpen(List<PBColumnInfo> open) {
		this.open = open;
	}

	public List<Integer> getNoCheCards() {
		return noCheCards;
	}

	public void setNoCheCards(List<Integer> noCheCards) {
		this.noCheCards = noCheCards;
	}

	public List<Integer> getNoChiCards() {
		return noChiCards;
	}

	public void setNoChiCards(List<Integer> noChiCards) {
		this.noChiCards = noChiCards;
	}

	public List<Integer> getChiCards() {
		return chiCards;
	}

	public void setChiCards(List<Integer> chiCards) {
		this.chiCards = chiCards;
	}

	public List<Integer> getNoHuCards() {
		return noHuCards;
	}

	public void setNoHuCards(List<Integer> noHuCards) {
		this.noHuCards = noHuCards;
	}

	/**
	 * 该玩家真正出过的牌，不包含翻开的牌
	 */
	public List<Integer> getChuCards() {
		return chuCards;
	}

	public void setChuCards(List<Integer> chuCards) {
		this.chuCards = chuCards;
	}

	public List<Integer> getNextChiCards() {
		return nextChiCards;
	}

	public void setNextChiCards(List<Integer> nextChiCards) {
		this.nextChiCards = nextChiCards;
	}

	public boolean isFive() {
		return five;
	}

	public boolean isZhaoChe() {
		return zhaoChe;
	}

	public void setZhaoChe(boolean zhaoChe) {
		this.zhaoChe = zhaoChe;
	}

	/**
	 * 本次可招吃的牌,招了从不能吃/che/胡列表删除
	 * 
	 * @return
	 */
	public List<Integer> getZhaoChiCards() {
		return zhaoChiCards;
	}

	public void setZhaoChiCards(List<Integer> zhaoChiCards) {
		this.zhaoChiCards = zhaoChiCards;
	}

	public void setFive(boolean five) {
		this.five = five;
	}

	public List<Integer> getNoChuCards() {
		return noChuCards;
	}

	public void setNoChuCards(List<Integer> noChuCards) {
		this.noChuCards = noChuCards;
	}

	public int getInitHoldSize() {
		return initHoldSize;
	}

	public void setInitHoldSize(int initHoldSize) {
		this.initHoldSize = initHoldSize;
	}

	public List<Integer> getChuListCards() {
		return chuListCards;
	}

	public void setChuListCards(List<Integer> chuListCards) {
		this.chuListCards = chuListCards;
	}

	public int getTuo() {
		return tuo;
	}

	public void setTuo(int tuo) {
		this.tuo = tuo;
	}

	public int getHuTuoNum() {
		return huTuoNum;
	}

	public void setHuTuoNum(int huTuoNum) {
		this.huTuoNum = huTuoNum;
	}

	public int getHuFanNum() {
		return huFanNum;
	}

	public void setHuFanNum(int huFanNum) {
		this.huFanNum = huFanNum;
	}

	public List<Integer> getChoiceChiCards() {
		return choiceChiCards;
	}

	public void setChoiceChiCards(List<Integer> choiceChiCards) {
		this.choiceChiCards = choiceChiCards;
	}

	public int getInitScore() {
		return initScore;
	}

	public void setInitScore(int initScore) {
		this.initScore = initScore;
	}

	public int getTouCard() {
		return touCard;
	}

	public void setTouCard(int touCard) {
		this.touCard = touCard;
	}

	public CSNotifySeatOperationChoice getChoice() {
		return choice;
	}

	public void setChoice(CSNotifySeatOperationChoice choice) {
		this.choice = choice;
	}

	public CSNotifyGameOver getGameOver() {
		return gameOver;
	}

	public void setGameOver(CSNotifyGameOver gameOver) {
		this.gameOver = gameOver;
	}

	public List<Integer> getDouble7s() {
		return double7s;
	}

	public void setDouble7s(List<Integer> double7s) {
		this.double7s = double7s;
	}

	public boolean isBaoZi() {
		return baoZi;
	}

	public void setBaoZi(boolean baoZi) {
		this.baoZi = baoZi;
	}

	public int getHuType() {
		return huType;
	}

	public void setHuType(int huType) {
		this.huType = huType;
	}

	public boolean isPiao() {
		return piao;
	}

	public void setPiao(boolean piao) {
		this.piao = piao;
	}

	public boolean isChoicePiao() {
		return choicePiao;
	}

	public void setChoicePiao(boolean choicePiao) {
		this.choicePiao = choicePiao;
	}

	public boolean isChoiceDang() {
		return choiceDang;
	}

	public void setChoiceDang(boolean choiceDang) {
		this.choiceDang = choiceDang;
	}

	public boolean isCanTianHu() {
		return canTianHu;
	}

	public void setCanTianHu(boolean canTianHu) {
		this.canTianHu = canTianHu;
	}

	public boolean isEnterRoom() {
		return enterRoom;
	}

	public void setEnterRoom(boolean enterRoom) {
		this.enterRoom = enterRoom;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getSigenNum() {
		return sigenNum;
	}

	public void setSigenNum(int sigenNum) {
		this.sigenNum = sigenNum;
	}

	public Map<Integer, Integer> getBaoFans() {
		return baoFans;
	}

	public void setBaoFans(Map<Integer, Integer> baoFans) {
		this.baoFans = baoFans;
	}

	public boolean isFeiTian25Pass() {
		return feiTian25Pass;
	}

	public void setFeiTian25Pass(boolean feiTian25Pass) {
		this.feiTian25Pass = feiTian25Pass;
	}

	public boolean isHuSiGen() {
		return huSiGen;
	}

	public void setHuSiGen(boolean huSiGen) {
		this.huSiGen = huSiGen;
	}

	public boolean isThisChuIsZhui() {
		return thisChuIsZhui;
	}

	public void setThisChuIsZhui(boolean thisChuIsZhui) {
		this.thisChuIsZhui = thisChuIsZhui;
	}

	public List<Integer> getGuoShouCards() {
		return guoShouCards;
	}

	public void setGuoShouCards(List<Integer> guoShouCards) {
		this.guoShouCards = guoShouCards;
	}

	public boolean isZhaoChiNoGe() {
		return zhaoChiNoGe;
	}

	public void setZhaoChiNoGe(boolean zhaoChiNoGe) {
		this.zhaoChiNoGe = zhaoChiNoGe;
	}

	public List<Integer> getCanChiHoldCards() {
		return canChiHoldCards;
	}

	public void setCanChiHoldCards(List<Integer> canChiHoldCards) {
		this.canChiHoldCards = canChiHoldCards;
	}

	public Map<Integer, List<Integer>> getCardNoChiList() {
		return cardNoChiList;
	}

	public void setCardNoChiList(Map<Integer, List<Integer>> cardNoChiList) {
		this.cardNoChiList = cardNoChiList;
	}

	public CSResponsePlayBack getPlayBack() {
		return playBack;
	}

	public void setPlayBack(CSResponsePlayBack playBack) {
		this.playBack = playBack;
	}

	public List<Integer> getNoChuZhuiCards() {
		return noChuZhuiCards;
	}

	public void setNoChuZhuiCards(List<Integer> noChuZhuiCards) {
		this.noChuZhuiCards = noChuZhuiCards;
	}

	public List<Integer> getDoubleZhuiCards() {
		return doubleZhuiCards;
	}

	public void setDoubleZhuiCards(List<Integer> doubleZhuiCards) {
		this.doubleZhuiCards = doubleZhuiCards;
	}

	public boolean isMoPai() {
		return moPai;
	}

	public void setMoPai(boolean moPai) {
		this.moPai = moPai;
	}

	public List<Integer> getKou() {
		return kou;
	}

	public void setKou(List<Integer> kou) {
		this.kou = kou;
	}

	public boolean isWanJiao() {
		return wanJiao;
	}

	public void setWanJiao(boolean wanJiao) {
		this.wanJiao = wanJiao;
	}

	public int getNumJiao() {
		return numJiao;
	}

	public void setNumJiao(int numJiao) {
		this.numJiao = numJiao;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

}
