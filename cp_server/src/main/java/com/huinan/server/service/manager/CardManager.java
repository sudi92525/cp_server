package com.huinan.server.service.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSNotifySeatOperationChoice;
import com.huinan.proto.CpMsgCs.ENActionType;
import com.huinan.proto.CpMsgCs.ENColType;
import com.huinan.proto.CpMsgCs.ENRoomType;
import com.huinan.proto.CpMsgCs.ENZhaoType;
import com.huinan.proto.CpMsgCs.PBColumnInfo;
import com.huinan.proto.CpMsgCs.PBColumnInfo.Builder;
import com.huinan.server.service.action.GameAction;
import com.huinan.server.service.data.BrandEnums;
import com.huinan.server.service.data.Card;
import com.huinan.server.service.data.Constant;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;

/**
 *
 * renchao
 */
public class CardManager {

	private static Logger log = LogManager.getLogger(CardManager.class);

	/**
	 * 用户初始化手牌数：头家18张，尾家5张，其余玩家17张
	 */
	public static Integer BRAND_NUMFOUR[] = { 18, 17, 17, 5 };

	public static List<Integer> zhuiCards = new ArrayList<>();

	public static List<Integer> allPais = new ArrayList<>();
	static {
		for (int i = 1; i <= 6; i++) {
			for (int j = i; j <= 6; j++) {
				int card = i * 10 + j;
				allPais.add(card);
			}
		}
		zhuiCards.add(66);
		zhuiCards.add(11);
		zhuiCards.add(56);
		zhuiCards.add(12);
		// for (int i = 1; i <= 6; i++) {
		// for (int j = i; j <= 6; j++) {
		// int card = i * 10 + j;
		// allPais.add(33);
		// }
		// }
	}

	/**
	 * 获取和目标牌点数相等的所有牌
	 * 
	 * @param card
	 * @return
	 */
	public static List<Integer> getSameCards(int card) {
		List<Integer> sameCards = new ArrayList<>();
		for (Integer integer : allPais) {
			if (getCardValue(card) == getCardValue(integer)) {
				sameCards.add(integer);
			}
		}
		return sameCards;
	}

	public static List<Integer> getOtherCardsOfHold(User user, int cardValue) {
		int value = 14 - cardValue;
		List<Integer> sameCards = new ArrayList<>();
		for (Integer integer : user.getHold()) {
			if (value == getCardValue(integer)) {
				sameCards.add(integer);
			}
		}
		return sameCards;
	}

	/**
	 * 获取和目标牌点数相等的黑牌牌
	 * 
	 * @param card
	 * @return
	 */
	public static List<Integer> getSameHeiCards(int card) {
		List<Integer> sameCards = new ArrayList<>();
		for (Integer integer : allPais) {
			if (!colorIsRed(integer)
					&& getCardValue(card) == getCardValue(integer)) {
				sameCards.add(integer);
			}
		}
		return sameCards;
	}

	/**
	 * 获取牌的颜色,1为红色,0为黑色
	 * 
	 * @param cardNum
	 * @return
	 */
	public static boolean colorIsRed(int cardNum) {
		return BrandEnums.getCodeNum(cardNum) == 1;
	}

	/**
	 * 判断五张是否是全黑(丁斧两边甩)
	 * 
	 * @param user
	 * @return
	 */
	public static boolean isAllBlack(User user, List<Integer> hold,
			List<Integer> open) {
		if (!user.isFive()) {
			return false;
		}
		if (!open.isEmpty()) {
			return false;
		} else {
			int dingFuTimes = 0;
			Room room = RoomManager.getInstance().getRoom(user.getRoomId());
			for (Integer num : hold) {
				if (room.isDingFuColor()) {
					if (num == 12) {
						dingFuTimes++;
					}
					if (num != 12 && num != 56 && colorIsRed(num)) {
						return false;
					}
				} else {
					if (colorIsRed(num)) {
						return false;
					}
				}
			}
			if (room.getRoomType() != ENRoomType.EN_ROOM_TYPE_XC_VALUE
					&& dingFuTimes > 1) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAllRed(User user, List<Integer> hold,
			List<Integer> open) {
		if (!user.isFive()) {
			return false;
		}
		if (!open.isEmpty()) {
			return false;
		} else {
			int dingFuTimes = 0;
			Room room = RoomManager.getInstance().getRoom(user.getRoomId());
			for (Integer num : hold) {
				if (room.isDingFuColor()) {
					if (num == 12) {
						dingFuTimes++;
					}
					if (num != 12 && num != 56 && !colorIsRed(num)) {
						return false;
					}
				} else {
					if (!colorIsRed(num)) {
						return false;
					}
				}
			}
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				return true;
			} else if (!room.isDingFuShuaiTimes() && dingFuTimes > 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 小家是否含有丁斧
	 * 
	 * @param user
	 * @return
	 */
	public static boolean isHasDingFu(User user) {
		List<Integer> hold = user.getHold();
		for (Integer num : hold) {
			if (num != 12) {
				return true;
			}
		}
		return false;
	}

	private static int fuTouNum(User user) {
		int fuTouNum = 0;
		for (Integer integer : user.getHold()) {
			if (integer == 56) {
				fuTouNum++;
			}
		}
		return fuTouNum;
	}

	/**
	 * 获取基础坨数(计算了全黑/全红 等特殊坨数)
	 * 
	 * @param hold
	 * @param open
	 * @return
	 */
	public static int tuoNum(User user, List<Integer> hold) {
		int tuo = 0;
		List<PBColumnInfo> open = user.getOpen();
		log.info("===========tuoNum===========");
		for (PBColumnInfo info : open) {
			tuo += info.getScore();
			log.info("tuoNum,open:+" + info.getScore());
		}
		int fuTouNum = 0;
		for (Integer integer : hold) {
			if (colorIsRed(integer)) {
				tuo += 1;
				log.info("tuoNum,hold:red card,+1" + ",牌:" + integer);
			}
			if (integer == 56) {
				fuTouNum++;
			}
		}
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		log.info("tuoNum,基本托数=" + tuo);
		log.info("tuoNum,hold:斧头个次,fuTouNum=" + fuTouNum);
		// 五张-全黑/全红=一番(5坨)
		if (user.isFive() && open.isEmpty()) {
			log.info("tuoNum,is five,no che tou");
			if (fuTouNum == 0) {
				if (tuo == 0) {
					tuo = 6;
					log.info("tuoNum,全黑无斧头，tuo = 6");
				} else if (tuo == 1) {
					if (room.isWuHeiYiHong()) {
						tuo = 5;
						log.info("tuoNum,五黑一红(不包含丁斧)，tuo = 5");
					}
				}
			} else if (fuTouNum == 1) {
				if (tuo == 1) {
					tuo = 6;
					log.info("tuoNum,全黑:有丁丁甩一次，tuo = 6");
				} else if (tuo == 2) {
					if (room.isWuHeiYiHong()) {
						tuo = 5;
						log.info("tuoNum,五黑一红(包含丁斧)，tuo = 5");
					} else {
						log.info("tuoNum,五红六黑，tuo = 2");
					}
				} else if (tuo == 4) {
					tuo = 5;
					log.info("tuoNum,五红一黑:有斧头甩一次，tuo = 5");
				} else if (tuo == 5) {
					tuo = 6;
					log.info("tuoNum,全红:有斧头甩一次，tuo = 6");
				}
			} else {// 多个斧头
				if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
						|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE
						|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_CX_VALUE) {
					tuo = 0;
					log.info("tuoNum,南充/西充/MY/苍溪，一对丁丁斧头不能割牌，tuo = 0");
					return tuo;
				} else if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
					if (tuo == 2) {// 2个丁丁，其他全黑
						tuo = 6;
						log.info("tuoNum,全黑:有丁丁甩两次，tuo = 6");
					} else if (tuo == 3) {// 2个丁丁，一个红的
						tuo = 0;
						log.info("tuoNum,西充有两个丁丁，只能胡全红全黑，tuo=0");
					} else if (tuo == 4) {// 2个丁丁，两个红的
						tuo = 6;
						log.info("tuoNum,全红:有斧头甩两次，tuo = 6");
					}
					return tuo;
				}
				if (room.isDingFuShuaiTimes()) {
					// tuo += 2;// 随便甩
					if (tuo == 2) {// 2个丁丁，其他全黑
						tuo = 6;
						log.info("tuoNum,全黑:有丁丁甩两次，tuo = 6");
					} else if (tuo == 3) {// 2个丁丁，一个红的
						tuo = 5;
						log.info("tuoNum,五红一黑:有斧头甩两次，tuo = 5");
					} else if (tuo == 4) {// 2个丁丁，两个红的
						tuo = 6;
						log.info("tuoNum,全红:有斧头甩两次，tuo = 6");
					}
				} else {
					tuo++;// 斧头个数大于1，甩一次，所以加一
					log.info("tuoNum,丁斧对数大于1，甩一次+1");
				}
			}
		} else if (user.isFive() && !open.isEmpty()) {// ----10.23--jia
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				tuo += fuTouNum;
			}
		}
		log.info("===========tuoNum===========");
		return tuo;
	}

	public static int fanNum(User user) {
		List<Integer> hold = user.getHold();
		List<PBColumnInfo> open = user.getOpen();
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		int tuo = tuoNum(user, hold);
		user.setHuTuoNum(tuo);
		int fan = 0;

		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE) {
			return fanNumMY(user, room);
		}
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE
				&& user.getSeatIndex() == room.getZhuangSeat()) {
			if (tuo >= Constant.TUO_ZHUANG_FAN) {
				fan++;
				log.info("庄家大胡，加1翻");
			}
		} else if (user.getSeatIndex() == room.getDangSeat()) {
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				if (tuo >= Constant.TUO_XIANJIA_FAN) {
					fan++;
					log.info("西充庄家大胡，加1翻");
				}
			} else {
				if (tuo >= Constant.TUO_ZHUANG_FAN) {
					fan++;
					log.info("庄家大胡，加1翻");
				}
			}
		} else if (user.isFive()) {
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE
					|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				if (tuo >= Constant.TUO_XIAOJIA_3_FAN) {
					fan += 3;
					log.info("五张大胡3翻，加3翻");
				} else if (tuo >= Constant.TUO_XIAOJIA_2_FAN) {
					fan += 2;
					log.info("五张大胡2翻，加2翻");
				} else if (tuo >= Constant.TUO_XIAOJIA_1_FAN) {
					fan++;
					log.info("五张大胡1翻，加1翻");
				}
			} else if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE) {
				if (tuo >= Constant.TUO_XIAOJIA_4_FAN_NC) {
					fan += 4;
					log.info("五张大胡4翻，加4翻");
				} else if (tuo >= Constant.TUO_XIAOJIA_3_FAN_NC) {
					fan += 3;
					log.info("五张大胡3翻，加3翻");
				} else if (tuo >= Constant.TUO_XIAOJIA_2_FAN) {
					fan += 2;
					log.info("五张大胡2翻，加2翻");
				} else if (tuo >= Constant.TUO_XIAOJIA_1_FAN) {
					fan++;
					log.info("五张大胡1翻，加1翻");
				}
			}
		} else {
			if (tuo >= Constant.TUO_XIANJIA_FAN) {
				fan++;
				log.info("闲家大胡1翻，加1翻");
			}
		}

		int blackKan = 0;// 几坎黑
		int che7Num = 0;
		for (PBColumnInfo info : open) {
			int type = info.getColType().getNumber();
			if (type == ENColType.EN_COL_TYPE_TOU_VALUE) {
				int card = info.getCards(0);
				if (getCardValue(card) == 7) {
					che7Num++;
				}
				if (info.getCardsCount() == 4) {
					if (room.getFanPais().contains(card)) {
						if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
							if (user.isFive()) {
								fan += 3;// 小家番牌龙
								log.info("GY小家偷番牌龙，加3翻");
							} else {
								fan += 2;// 普通家番牌龙
								log.info("GY普通家偷番牌龙，加2翻");
							}
						} else {
							if (info.getIsQishouTou()) {
								if (user.isFive()) {
									fan += 2;// 小家番牌龙
									log.info("NC/XN小家起手偷番牌龙，加2翻");
								} else {
									fan += 2;// 普通家番牌龙
									log.info("NC/XN普通家起手偷番牌龙，加2翻");
								}
							} else {
								if (user.isFive()) {
									fan += 1;// 小家番牌龙
									log.info("NC/XN小家中间偷番牌龙，加1翻");
								} else {
									fan += 2;// 普通家番牌龙
									log.info("NC/XN普通家中间偷番牌龙，加2翻");
								}
							}
						}
					} else {
						if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
							if (user.isFive()) {// 小家普通牌龙
								fan += 2;
								log.info("GY小家偷普通牌龙，加2翻");
							} else {
								fan++;
								log.info("GY普通家偷普通牌龙，加1翻");
							}
						} else {
							if (user.isFive()) {
								fan += 1;// 小家番牌龙
								log.info("NC/XN小家偷普通牌龙，加1翻");
							} else {
								fan += 1;// 普通家番牌龙
								log.info("NC/XN普通家偷普通牌龙，加1翻");
							}
						}
					}
				} else {
					if (room.getFanPais().contains(card)) {
						if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
							if (user.isFive()) {// 小家偷番牌
								fan += 2;
								log.info("GY小家偷番牌，加2翻");
							} else {
								fan += 1;
								log.info("GY普通家偷番牌，加1翻");
							}
						} else {
							if (user.isFive()) {// 小家偷番牌
								fan += 1;
								log.info("NC/XN小家偷番牌，加1翻");
							} else {
								fan += 1;
								log.info("NC/XN普通家偷番牌，加1翻");
							}
						}
					} else {
						if (user.isFive()
								&& room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {// 小家普通牌
							fan += 1;
							log.info("GY小家偷普通牌，加1翻");
						}
					}
				}
				if (!colorIsRed(card)) {
					blackKan++;
				}
			} else if (type == ENColType.EN_COL_TYPE_PENG_VALUE) {
				int card = info.getCards(0);
				if (getCardValue(card) == 7) {
					che7Num++;
				}
				if (room.getFanPais().contains(card)) {
					if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
						if (user.isFive()) {// 小家扯番牌
							fan += 2;
							log.info("GY小家扯番牌，加2翻");
						} else {
							fan++;
							log.info("GY普通家扯番牌，加1翻");
						}
					} else {
						if (user.isFive()) {// 小家扯番牌
							fan += 1;
							log.info("NC/XN小家扯番牌，加1翻");
						} else {
							fan++;
							log.info("NC/XN普通家扯番牌，加1翻");
						}
					}
				} else {
					if (user.isFive()
							&& room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {// 小家扯番牌
						fan += 1;
						log.info("GY小家扯普通牌，加1翻");
					}
				}
				if (!colorIsRed(card)) {
					blackKan++;
				}
			}
		}
		if (user.isFive()) {
			if (isAllRed(user, hold, user.getOpenList())) {
				if (hold.contains(12)) {// 包含丁丁斧头的全黑全红
					if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
							|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
						fan = 1;
						log.info("小家全红（包含丁斧），南充西充算1番");
					} else {
						fan = 0;
						log.info("小家全红（包含丁斧），不算番");
					}
				} else {
					fan = 1;
					log.info("小家全红（不含丁斧），算1番");
				}
			} else if (isAllBlack(user, hold, user.getOpenList())) {
				if (hold.contains(12)) {// 包含丁丁斧头的全黑全红
					if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
							|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
						fan = 1;
						log.info("小家全黑（包含丁斧），南充西充算1番");
					} else {
						fan = 0;
						log.info("小家全黑（包含丁斧），不算番");
					}
				} else {
					fan = 1;
					if (room.isBlackTwoFan()) {
						fan = 2;
					}
					log.info("小家全黑（不含丁斧），算" + fan + "番");
				}
			}
			if (room.isZiMoJiaFan()) {// 小家自摸加番
				if (room.getCurrentCard().getSeat() == user.getSeatIndex()) {
					fan += 1;
					log.info("小家自摸加番，加" + 1 + "番");
				}
			}
		} else {
			// 三砍黑算一番(包括黑龙)
			if (blackKan >= 3) {
				fan++;
				log.info("三坎黑，加1翻");
			}
		}
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
			if (!user.getBaoFans().isEmpty()) {
				for (Integer num : user.getBaoFans().values()) {
					fan += num;
					log.info("广元吃成四根,加" + num + "翻");
				}
			}
			// 计算四根的个数
			int siGenNum = 0;
			for (PBColumnInfo info : open) {
				int type = info.getColType().getNumber();
				if (type == ENColType.EN_COL_TYPE_TOU_VALUE
						|| type == ENColType.EN_COL_TYPE_PENG_VALUE) {
					int card = info.getCards(0);
					if (user.getHold().contains(Integer.valueOf(card))) {
						siGenNum++;
						log.info("广元手里的四根,加" + 1 + "翻");
					}
				}
			}
			fan += siGenNum;
		}
		if (room.isCheAll7Fan() && che7Num == 3) {
			fan++;
			log.info("扯了全部7点,加" + 1 + "翻");
		}
		log.info("总番=" + fan);
		// 限番
		if (room.getRoomTable().getHighTimes() != -1
				&& fan > room.getRoomTable().getHighTimes()) {
			fan = room.getRoomTable().getHighTimes();
			log.info("超出最大番数=" + fan);
		}
		log.info("最终番数=" + fan);
		return fan;
	}

	private static int fanNumMY(User user, Room room) {
		int fan = 0;
		// 小家自摸加番
		if (user.isFive() && room.isZiMoJiaFan()
				&& room.getCurrentCard().getSeat() == user.getSeatIndex()) {
			fan += 1;
			log.info("MY,小家自摸加番，加" + 1 + "番");
		}
		return fan;
	}

	/** <pai,个数> */
	public static Map<Integer, Integer> toMap(List<Integer> hold) {
		Map<Integer, Integer> map = new HashMap<>();
		for (Integer integer : hold) {
			if (map.containsKey(integer)) {
				map.put(integer, map.get(integer) + 1);
			} else {
				map.put(integer, 1);
			}
		}
		return map;
	}

	/**
	 * 计算玩家可操作列表（出牌，翻牌）
	 * 
	 * @param room
	 * @param chuUser
	 */
	public static boolean logicActionList(Room room, User chuUser,
			boolean isFiveMo) {
		boolean che7 = false;
		room.getCanActionSeat().clear();
		Card card = room.getCurrentCard();
		Map<Integer, User> users = room.getUsers();
		if (isFiveMo || card.isCheMo()) {// 五张摸起来的,只计算自己
			logicUserActionList(room, card, users.get(card.getSeat()),
					isFiveMo, true);
		} else {
			// 飞天二五
			boolean isFeiTian25 = feiTian25(room, chuUser, isFiveMo, card,
					users);
			if (isFeiTian25) {
				return false;// -------10.7增加，自动che7后不往下走-----
			}
			for (User user : users.values()) {
				if (card.isChu() && user.getUuid().equals(chuUser.getUuid())) {
					continue;
				}
				che7 = logicUserActionList(room, card, user, false, false);
				if (che7) {
					break;
				}
			}
			if (!che7) {
				for (User user : users.values()) {
					if (card.isChu()
							&& user.getUuid().equals(chuUser.getUuid())) {
						continue;
					}
					if (!user.getActions().isEmpty()) {
						notifyChoice(room, card, user);
					}
				}
			}
		}
		return che7;// -------10.7增加，自动che7后不往下走-----
	}

	private static boolean feiTian25(Room room, User chuUser, boolean isFiveMo,
			Card card, Map<Integer, User> users) {
		for (User user : users.values()) {
			if (card.isChu() && user.getUuid().equals(chuUser.getUuid())) {
				continue;
			}
			if (user.isFive() && card.getNum() == 25 && !user.isFeiTian25Pass()) {
				boolean hu = isHu(user, card, isFiveMo);
				if (hu) {
					room.getCanHuSeat().add(user.getSeatIndex());
					user.getActions().add(ENActionType.EN_ACTION_HU);
					int huType = 0;
					if (user.getSeatIndex() == card.getSeat()) {
						huType = 1;// 自摸
					} else if (card.isChu()) {
						huType = 2;// 点炮
					} else {
						huType = 3;// 别人翻开的
					}
					user.setHuType(huType);
					user.setFeiTian25Pass(false);
					card.setFeiTian25(true);

					notifyChoice(room, card, user);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 计算user可操作列表（出牌，翻牌，偷牌）
	 * 
	 * @param room
	 * @param card
	 * @param user
	 * @param isFiveMo
	 */
	public static boolean logicUserActionList(Room room, Card card, User user,
			boolean isFiveMo, boolean sendNotify) {
		boolean hu = isHu(user, card, isFiveMo);
		if (hu) {
			room.getCanHuSeat().add(user.getSeatIndex());
			user.getActions().add(ENActionType.EN_ACTION_HU);
			int huType = 0;
			if (user.getSeatIndex() == card.getSeat()) {
				if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_MY_VALUE
						&& card.isCheMo()) {
					huType = 2;// MY点炮
				} else {
					huType = 1;// 自摸
				}
			} else if (card.isChu()) {
				huType = 2;// 点炮
			} else {
				huType = 3;// 别人翻开的
			}
			user.setHuType(huType);
		}
		if (isChe(room, user, card)) {
			// !user.isFive()&& (card.isCheMo() || card.isChu()&&
			// card.getCardValue() == 7)
			if (!user.isFive() && card.getCardValue() == 7) {
				// 扯了又可以扯:自动偷(扯起来的有偷必偷),排除五张::::走的checkUserTou()
				// 7点自动扯:通知扯7
				GameAction.che(user, room);
				// 清理其他玩家的actions
				for (User _user : room.getUsers().values()) {
					if (!_user.getUuid().equals(user.getUuid())
							&& !_user.getActions().isEmpty()) {
						_user.getActions().clear();
					}
				}
				return true;
			}
			room.setCanCheSeat(user.getSeatIndex());
			user.getActions().add(ENActionType.EN_ACTION_PENG);
		}
		if (!card.isCheMo() && user.getHold().size() > 1) {// 扯起来的不管吃
			if (isChi(user, card)) {
				room.getCanChiSeat().add(user.getSeatIndex());
				room.getCanChiSeatTemp().add(user.getSeatIndex());
				user.getActions().add(ENActionType.EN_ACTION_CHI);
			}
		}
		if (sendNotify && user.getActions().size() != 0) {
			notifyChoice(room, card, user);
		}
		return false;
	}

	public static boolean checkTianHu(Room room, User zhuang) {
		Card card = new Card(zhuang.getHold().get(0), zhuang.getSeatIndex(),
				false, false, true, true);
		if (isHu(zhuang, card, true)) {
			room.setCurrentCard(card);
			zhuang.setCanTianHu(true);
			room.getCanHuSeat().add(zhuang.getSeatIndex());
			zhuang.getActions().add(ENActionType.EN_ACTION_HU);
			zhuang.setHuType(1);
			// 发送位置通知
			NotifyHandler.notifyNextOperation(room, zhuang.getSeatIndex());
			// 通知是否胡
			notifyChoice(room, card, zhuang);
			return true;
		}
		return false;
	}

	public static void notifyChoice(Room room, Card card, User user) {
		user.getActions().add(ENActionType.EN_ACTION_GUO);
		room.getCanActionSeat().add(user.getSeatIndex());

		// 可操作列表推送
		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSNotifySeatOperationChoice.Builder choice = ProtoBuilder.buildChoice(
				card, user);
		msg.setCsNotifySeatOperationChoice(choice);
		user.setChoice(choice.build());
		NotifyHandler.notifyOne(user.getUuid(),
				CpMsgData.CS_NOTIFY_SEAT_OPERATION_CHOICE_FIELD_NUMBER,
				msg.build());
	}

	public static List<Integer> getCanChiList(User user, Card card) {
		List<Integer> chiList = new ArrayList<>();
		for (Integer integer : user.getHold()) {
			if (getCardValue(integer) + card.getCardValue() == 14
					&& !chiList.contains(integer)
					&& !user.getDouble7s().contains(integer)
					&& !user.getDoubleZhuiCards().contains(integer)) {
				chiList.add(integer);
			}
		}
		return chiList;
	}

	/**
	 * 能否扯
	 * 
	 * @param room
	 * 
	 * @param user
	 * @param card
	 * @return
	 */
	public static boolean isChe(Room room, User user, Card card) {
		if (user.getNoCheCards().contains(Integer.valueOf(card.getNum()))) {
			return false;
		}
		Map<Integer, Integer> map = toMap(user.getHold());
		Integer num = map.get(Integer.valueOf(card.getNum()));
		int needNum = 2;
		if (!card.isChu()) {// 翻开的
			if (user.isFive() && card.getSeat() == user.getSeatIndex()
					|| card.isCheMo() && card.getSeat() == user.getSeatIndex()) {
				needNum = 3;
			}
		}
		if (num != null && num >= needNum) {
			return true;
		}
		return false;
	}

	/**
	 * 能否吃
	 * 
	 * @param user
	 * @param card
	 * @return
	 */
	public static boolean isChi(User user, Card card) {
		// 判断位置
		int nextSeat = RoomManager.getNextSeat(card.getSeat());
		if (user.isFive()) {//
			return false;
		}
		if (card.isChu()) {
			if (user.getSeatIndex() != nextSeat) {
				return false;
			}
		} else {
			if (user.getSeatIndex() != card.getSeat()
					&& user.getSeatIndex() != nextSeat) {
				return false;
			}
		}
		if (user.getNoChiCards().contains(card.getNum())) {
			return false;
		}

		for (Integer integer : user.getHold()) {
			if (getCardValue(integer) + card.getCardValue() == 14
					&& !user.getDouble7s().contains(integer)
					&& !user.getDoubleZhuiCards().contains(integer)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 能否胡
	 * 
	 * @param user
	 * @param card
	 * @param isFiveMo
	 * @return
	 */
	public static boolean isHu(User user, Card card, boolean isFiveMo) {
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		log.info("==========isHu============seat=" + user.getSeatIndex());
		if (user.isZhaoChiNoGe()) {
			log.info("isHu,false:南充西充,招了不能割");
			return false;
		}
		if (card == null) {
			return false;
		}
		if (!card.isCheMo() && !isFiveMo
				&& user.getNoHuCards().contains(card.getNum())) {
			log.info("isHu,false:不是扯投,该牌在不能胡的牌列表中");
			return false;
		}
		// TODO 单独加选项:不吃不割,和吃红打黑二选一
		// if (room.isChiHongDaHei()) {
		// // 吃红打黑：不吃不割：不能吃的不能割-------9.14---增
		// if (!user.isFive() && !card.isCheMo()
		// && user.getNoChiCards().contains(card.getNum())) {
		// log.info("isHu,false:吃红打黑：不吃不割,除非扯投");
		// return false;
		// }
		// }
		List<Integer> newHold = new ArrayList<>();
		newHold.addAll(user.getHold());
		if (!card.isCheMo() && !isFiveMo || card.isChu()) {
			newHold.add(card.getNum());
		}
		if (newHold.size() % 2 != 0) {
			log.info("isHu,false:牌数量不对:" + newHold.size());
			return false;// 牌不是偶数张
		}
		// 最后判断是否斗够十四
		boolean dou14 = checkDou14(newHold);
		if (!dou14) {
			return false;
		}
		boolean tuoNumGou = checkTuoNum(room, user, newHold);
		if (!tuoNumGou) {
			return false;
		}
		if (card.getCardValue() == 7) {// 如果是7点,判断是否能胡
			boolean canHu7 = checkHu7(user, card);
			if (!canHu7) {
				log.info("isHu,false:不是胡的七点:" + card.getNum());
				return false;
			}
		}
		log.info("==========is Hu=true============");
		return true;
	}

	public static boolean isZhao(Room room, User user) {
		if (user.isFive()) {
			return false;
		}
		Card card = room.getCurrentCard();
		int countOfHold = getCardCountOfHold(user, card.getNum());
		// int countOfReset = getCardCountOfReset(room, card.getNum());
		int countOfAllChu = CardManager.getCardCountOfAllChu(room, user,
				card.getNum());
		if (card.getCardValue() == 7 && countOfHold == 2) {// 一对七，不用招吃，自动报招扯
			if (countOfAllChu >= 1) {
				user.getNoCheCards().remove(Integer.valueOf(card.getNum()));
				List<Integer> cards = new ArrayList<>();
				user.setZhaoChe(false);
				cards.add(card.getNum());
				cards.add(card.getNum());
				cards.add(card.getNum());
				Builder col = ProtoBuilder.buildPBColumnInfo(user, cards,
						ENColType.EN_COL_TYPE_ZHAO, false);
				// 3 通知招牌数据信息
				NotifyHandler.notifyActionFlowZhao(room, user, card,
						col.build(), ENActionType.EN_ACTION_ZHAO, false,
						ENZhaoType.EN_ZHAO_TYPE_CHE);
			}
			return false;
		}
		boolean zhao = false;
		if (countOfHold == 2 && countOfAllChu >= 1) {
			user.getNoCheCards().add(Integer.valueOf(card.getNum()));// 不招就不能扯
			user.setZhaoChe(true);
			zhao = true;
		}

		// 招吃:之前牌桌打出或翻出的某种牌没有吃，或则自己打出过某种牌，因为偷牌上手后，想同样的牌打出或翻出时吃，需要“招吃”。
		List<Integer> noChiList = user.getNoChiCards();
		List<Integer> zhaoChiList = user.getZhaoChiCards();
		for (Integer integer : noChiList) {
			if (CardManager.getCardValue(integer) + card.getCardValue() == 14) {
				// zhao chi
				if (!zhaoChiList.contains(integer)) {
					zhaoChiList.add(integer);
					zhao = true;
					break;
				}
			}
		}
		// 自己的出牌列表
		List<Integer> chuList = user.getChuListCards();
		if (!chuList.isEmpty()) {
			for (Integer integer : chuList) {
				if (CardManager.getCardValue(integer) + card.getCardValue() == 14) {
					// zhao chi
					if (!zhaoChiList.contains(integer)) {
						zhaoChiList.add(integer);
						zhao = true;
						break;
					}
				}
			}
		}
		// 上家打过,需要招
		List<Integer> lastChuList = getLastChuList(user);
		if (!lastChuList.isEmpty()) {
			for (Integer integer : lastChuList) {
				if (CardManager.getCardValue(integer) + card.getCardValue() == 14) {
					// zhao chi
					if (!zhaoChiList.contains(integer)) {
						zhaoChiList.add(integer);
						zhao = true;
						break;
					}
				}
			}
		}
		if (zhao) {
			user.getActions().add(ENActionType.EN_ACTION_ZHAO);
		}
		return zhao;
	}

	/**
	 * 是否吃退
	 * 
	 * @param room
	 * @param user
	 * @return
	 */
	public static boolean isChiTui(Room room, User user) {
		if (user.isFive()) {
			return false;
		}
		Card card = room.getCurrentCard();
		// 如果该牌是红色,判断是否吃过该点数的红色牌,吃过则需要吃退这一张牌
		int countOfChi = getCardCountOfChi(user, card.getNum());
		if (card.getCardValue() == 7) {// 920晚上：一对七，不用吃退
			int countOf7 = getCardCountOfHold(user, card.getNum());
			if (countOf7 == 2) {
				return false;
			}
		}
		boolean chdhTui = isChiSameValueRedCard(room, user, card.getNum());
		boolean ncChiTui = checkNCChiTui(room, user, card.getNum());
		if (ncChiTui) {
			user.getActions().add(ENActionType.EN_ACTION_TUI);
			room.getCanActionSeat().add(user.getSeatIndex());
			return true;
		} else if (chdhTui || (card.isCheMo() && countOfChi > 0)) {
			// 别人打得我吃了的牌
			CardManager.addToDeathCard(card.getNum(), user);
			user.getActions().add(ENActionType.EN_ACTION_TUI);
			room.getCanActionSeat().add(user.getSeatIndex());
			return true;
		}
		return false;
	}

	/**
	 * 南充西充同点数可以相互吃退
	 * 
	 * @param room
	 * @param user
	 * @param card
	 * @return
	 */
	private static boolean checkNCChiTui(Room room, User user, Integer card) {
		boolean tui = false;
		if (room.getRoomType() != ENRoomType.EN_ROOM_TYPE_NC_VALUE
				&& room.getRoomType() != ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
			return tui;
		}
		int cardValue = getCardValue(card);
		for (Integer integer : user.getNoChuCards()) {
			if (getCardValue(integer) == cardValue
					&& !user.getDouble7s().contains(integer)) {
				tui = true;
				break;
			}
		}
		return tui;
	}

	public static boolean checkDou14(List<Integer> newHold) {
		List<Integer> indexRecord = new ArrayList<>();
		for (int i = 0; i < newHold.size(); i++) {
			if (indexRecord.contains(i)) {
				continue;
			}
			for (int j = 0; j < newHold.size(); j++) {
				if (indexRecord.contains(j)) {
					continue;
				}
				if (i != j
						&& (CardManager.getCardValue(newHold.get(i)) + CardManager
								.getCardValue(newHold.get(j))) == 14) {
					indexRecord.add(i);
					indexRecord.add(j);
					break;
				}
			}
		}
		if (indexRecord.size() != newHold.size()) {
			log.info("isHu,false:不能斗十四");
			return false;
		}
		return true;
	}

	public static boolean checkTuoNum(Room room, User user,
			List<Integer> newHold) {
		int tuo = tuoNum(user, newHold);
		// TODO 写死牌
		// int tuo = 100;
		log.info("isHu,tuoNum=" + tuo);
		// 判断坨数是否够
		if (user.isFive()) {
			if (tuo < Constant.hu_wj_score) {
				log.info("isHu,false:小家坨数不够5");
				return false;
			}
		} else {
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
					|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				if (user.getSeatIndex() == room.getDangSeat()) {// 当家20坨
					if (tuo < Constant.hu_zj_score) {
						log.info("isHu,false:NC当家坨数不够20");
						return false;
					}
				} else {
					if (tuo < Constant.hu_xj_score) {
						log.info("isHu,false:NC普通家坨数不够18");
						return false;
					}
				}
			} else if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_GY_VALUE) {
				if (tuo < Constant.hu_xj_score) {
					log.info("isHu,false:GY普通家坨数不够18");
					return false;
				} else if (user.getSeatIndex() == room.getZhuangSeat()
						&& tuo < Constant.hu_zj_score) {
					log.info("isHu,false:GY庄家坨数不够20");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 判断是否能胡7点(25特殊)
	 * 
	 * @param user
	 * @param card
	 * @param isFirstCard
	 * @param isCheMo
	 * @return
	 */
	private static boolean checkHu7(User user, Card card) {
		// if (user.getHold().size() == 1) {
		// return false;
		// }
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		if (user.isFive()) {
			if (isAllBlack(user, user.getHold(), user.getOpenList())
					&& fuTouNum(user) == 0 && card.getNum() == 25) {// 飞天二五：全黑，无丁斧
				return true;
			}
			if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
					|| room.getRoomType() == ENRoomType.EN_ROOM_TYPE_XC_VALUE) {
				// if (card.isFirstCard() && card.getNum() == 25) {// 头家第一手打出25
				// return true;
				// }
				if (!user.isMoPai()) {
					return true;
				}
			}
			if (card.getSeat() == user.getSeatIndex()) {// 小家自摸七点
				return true;
			}
		} else {
			if (card.isCheMo()) {// 扯头
				return true;
			}
		}
		return false;
	}

	public static int getScoreByFan(int initScore, boolean addFan, int fanNum) {
		if (addFan) {
			return initScore + initScore * fanNum;
		} else {
			return initScore * (int) Math.pow(2, fanNum);
		}
	}

	public static void checkBaoZiOrChuPai(Room room, User user) {
		boolean huang = RoomManager.checkHuang(room, user,
				room.getCurrentCard(), room.getResetCards().size());
		if (huang) {
			return;
		}
		boolean baoZi = true;
		// 死牌个数==手牌个数：包子
		Map<Integer, Integer> holdMap = toMap(user.getHold());
		Map<Integer, Integer> noChuMap = toMap(user.getNoChuCards());
		for (Integer integer : user.getHold()) {
			Integer holdNum = holdMap.get(integer);
			Integer noChuNum = noChuMap.get(integer);
			if (noChuNum == null || holdNum > noChuNum) {
				baoZi = false;
				break;
			}
		}
		boolean ncBaoZi = checkZhui(room, user);
		if (baoZi || ncBaoZi) {
			log.info("包子");
			room.setHuSeat(user.getSeatIndex());
			user.setBaoZi(true);
			user.setHuType(4);
			room.setBaoZiSeat(user.getSeatIndex());
			RoomManager.total(room);
		} else {
			NotifyHandler.notifyChuPai(user);
			if (room.isStartChu()) {
				room.setCurrentCard(null);
			}
		}
	}

	/**
	 * 该我出牌的时候,判断是否有牌要追:只有扯过.手里有单着的,必追完
	 * 
	 * @param room
	 * @param user
	 * 
	 */
	public static boolean checkZhui(Room room, User user) {
		if (user.isFive()) {
			return false;
		}
		if (!room.isCheZhui() && !room.isDiaoZhui()) {
			return false;
		}
		List<Integer> deadCards = new ArrayList<>();
		deadCards.addAll(user.getHold());
		Map<Integer, Integer> holdMap = toMap(user.getHold());

		boolean baoZi = false;
		boolean zhui = false;
		Integer chuCard = null;
		if (room.isDiaoZhui()) {
			for (int i = 0; i < zhuiCards.size(); i++) {
				int zhuiCard = zhuiCards.get(i);
				Integer count = holdMap.get(zhuiCard);
				if (count != null && count == 1) {
					Integer card = Integer.valueOf(zhuiCard);

					int otherCard = 0;
					if (zhuiCard == 66) {
						otherCard = 11;
					} else if (zhuiCard == 11) {
						otherCard = 66;
					} else if (zhuiCard == 56) {
						otherCard = 12;
					} else if (zhuiCard == 12) {
						otherCard = 56;
					}
					if (otherCard != 0) {
						Integer other = holdMap.get(Integer.valueOf(otherCard));
						if (other == null) {
							zhui = true;
							deadCards.remove(card);
							chuCard = card;
							user.setThisChuIsZhui(true);
							NotifyHandler
									.notifyDeathCardOfZhui(user, deadCards);
							break;
						}
					}
				}
			}
		} else if (room.isCheZhui()) {
			out: for (int i = 0; i < zhuiCards.size(); i++) {
				int zhuiCard = zhuiCards.get(i);
				for (User _user : room.getUsers().values()) {
					for (PBColumnInfo col : _user.getOpen()) {
						if (col.getColType() == ENColType.EN_COL_TYPE_PENG
								|| col.getColType() == ENColType.EN_COL_TYPE_TOU) {
							Integer cardNum = col.getCardsList().get(0);
							if (zhuiCard == cardNum) {// 扯了的
								int otherCard = 0;
								if (zhuiCard == 66) {
									otherCard = 11;
								} else if (zhuiCard == 11) {
									otherCard = 66;
								} else if (zhuiCard == 56) {
									otherCard = 12;
								} else if (zhuiCard == 12) {
									otherCard = 56;
								}
								if (otherCard != 0) {
									Integer card = Integer.valueOf(otherCard);
									Integer count = holdMap.get(card);

									Integer _count = holdMap.get(cardNum);
									if (count != null && count == 1
											&& _count == null) {
										zhui = true;
										deadCards.remove(card);
										chuCard = card;

										user.setThisChuIsZhui(true);
										NotifyHandler.notifyDeathCardOfZhui(
												user, deadCards);
										break out;
									}
								}
							}
						}
					}
				}
			}
		}
		if (zhui && !deadCards.isEmpty()) {
			user.setNoChuZhuiCards(deadCards);
		}
		// ----10.11加:南充追牌和死牌冲突,,,,包子----
		if (chuCard != null && user.getNoChuCards().contains(chuCard)) {
			baoZi = true;
		}
		return baoZi;
	}

	public static PBColumnInfo buildCol(User user, int card, int num,
			int chiCard, ENColType type, boolean isCHi) {
		List<Integer> cards = new ArrayList<>();
		if (isCHi) {
			cards.add(card);
			cards.add(chiCard);
		} else {
			for (int i = 0; i < num; i++) {
				cards.add(card);
			}
		}
		Builder columuInfo = ProtoBuilder.buildPBColumnInfo(user, cards, type,
				false);
		return columuInfo.build();
	}

	public static boolean isHuSiGen(Room room, User user, Card huCard) {

		return false;
	}

	/**
	 * 吃红打黑:是否吃过同点数的红牌
	 * 
	 * @param room
	 * 
	 * @param user
	 * @param card
	 * @return
	 */
	public static boolean isChiSameValueRedCard(Room room, User user,
			Integer card) {
		if (!room.isChiHongDaHei()) {
			return false;
		}
		for (Integer integer : user.getChiCards()) {
			if (getCardValue(integer) == getCardValue(card)) {
				if (colorIsRed(integer)) {
					if (colorIsRed(card)) {
						// addToDeathCard(card, user);
						return true;
					}
				} else {
					// addToDeathCard(card, user);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 得到该牌的总数（手里和扯牌列表）
	 * 
	 * @param user
	 * @param seat
	 * @param card
	 * @return
	 */
	public static int getCardCountOfAll(User user, Integer card) {
		Map<Integer, Integer> tempCard = toMap(user.getHold());
		for (PBColumnInfo col : user.getOpen()) {
			for (Integer cardNum : col.getCardsList()) {
				if (tempCard.get(cardNum) != null) {
					tempCard.put(cardNum, tempCard.get(cardNum) + 1);
				} else {
					tempCard.put(cardNum, 1);
				}
			}
		}
		// -----------9.14下午注释-------------
		for (Integer cardNum : user.getChuCards()) {
			if (tempCard.get(cardNum) != null) {
				tempCard.put(cardNum, tempCard.get(cardNum) + 1);
			} else {
				tempCard.put(cardNum, 1);
			}
		}
		if (tempCard.get(card) != null) {
			return tempCard.get(card);
		} else {
			return 0;
		}
	}

	public static boolean getCardIsChe(User user, Integer card) {
		for (PBColumnInfo col : user.getOpen()) {
			if (col.getColType() == ENColType.EN_COL_TYPE_PENG
					|| col.getColType() == ENColType.EN_COL_TYPE_TOU) {
				return true;
			}
		}
		return false;
	}

	public static int getCardCountOfReset(Room room, Integer card) {
		Map<Integer, Integer> tempCard = toMap(room.getResetCards());
		if (tempCard.get(card) != null) {
			return tempCard.get(card);
		} else {
			return 0;
		}
	}

	public static int getCardCountOfNoChu(User user, Integer card) {
		Map<Integer, Integer> tempCard = toMap(user.getNoChuCards());
		if (tempCard.get(card) != null) {
			return tempCard.get(card);
		} else {
			return 0;
		}
	}

	public static int getCardCountOfChi(User user, Integer card) {
		Map<Integer, Integer> tempCard = toMap(user.getChiCards());
		if (tempCard.get(card) != null) {
			return tempCard.get(card);
		} else {
			return 0;
		}
	}

	public static int getCardCountOfHold(User user, Integer card) {
		Map<Integer, Integer> tempCard = toMap(user.getHold());
		if (tempCard.get(card) != null) {
			return tempCard.get(card);
		} else {
			return 0;
		}
	}

	public static int getCardCountOfChu(User user, Integer card) {
		Map<Integer, Integer> tempCard = toMap(user.getChuCards());
		if (tempCard.get(card) != null) {
			return tempCard.get(card);
		} else {
			return 0;
		}
	}

	public static List<Integer> getLastChuList(User user) {
		int lastSeat = RoomManager.getLastSeatIndex(user);
		Room room = RoomManager.getInstance().getRoom(user.getRoomId());
		User lastUser = room.getUsers().get(Integer.valueOf(lastSeat));
		return lastUser.getChuListCards();
	}

	/**
	 * 所有人过的牌:除了手牌，桌面上看得到的牌
	 * 
	 * @param user
	 * @param card
	 * @return
	 */
	public static int getCardCountOfAllChu(Room room, User user, Integer card) {
		Map<Integer, Integer> map = new HashMap<>();
		for (User _user : room.getUsers().values()) {
			// 自己手里拿下来吃的牌也算
			// ----------10.06加:-----------
			if (_user.getUuid().equals(user.getUuid())) {
				for (PBColumnInfo col : _user.getOpen()) {
					for (Integer cardNum : col.getCardsList()) {
						if (map.get(cardNum) != null) {
							map.put(cardNum, map.get(cardNum) + 1);
						} else {
							map.put(cardNum, 1);
						}
					}
				}
			}
			for (Integer integer : _user.getGuoShouCards()) {
				if (map.containsKey(integer)) {
					map.put(integer, map.get(integer) + 1);
				} else {
					map.put(integer, 1);
				}
			}
			for (Integer integer : _user.getChuListCards()) {
				if (map.containsKey(integer)) {
					map.put(integer, map.get(integer) + 1);
				} else {
					map.put(integer, 1);
				}
			}
		}
		if (map.get(card) != null) {
			return map.get(card);
		} else {
			return 0;
		}
	}

	public static int getScore(User user, List<Integer> cards) {
		int score = 0;
		if (cards.size() == 2) {
			for (Integer card : cards) {
				boolean red = colorIsRed(card);
				if (red) {
					score++;
				}
			}
		} else if (cards.size() == 3) {
			boolean red = colorIsRed(cards.get(0));
			if (red) {
				score = Constant.tou_3_hong;
			} else {
				score = Constant.tou_3_hei;
			}
		} else if (cards.size() == 4) {
			boolean red = colorIsRed(cards.get(0));
			if (red) {
				score = Constant.tou_4_hong;
			} else {
				score = Constant.tou_4_hei;
			}
		}
		return score;
	}

	public static int getCardValue(int cardNum) {
		return cardNum / 10 + cardNum % 10;
	}

	/**
	 * 不能出一对7,吊追单独一对不能拆开打
	 * 
	 * @param user
	 * @param isDealCard
	 *            是否是发牌,发牌时不在这里发推送,后面统一发
	 */
	public static void noChuDouble7AndDiaoZhui(Room room, User user,
			boolean isDealCard) {
		if (user.isFive()) {
			return;
		}
		// Map<Integer, Integer> noChuMap = toMap(user.getNoChuCards());
		Map<Integer, Integer> holdMap = toMap(user.getHold());
		Iterator<Integer> it = holdMap.keySet().iterator();
		boolean send = false;
		while (it.hasNext()) {
			Integer card = (Integer) it.next();
			int count = holdMap.get(card);
			if (count == 2 && getCardValue(card) == 7) {
				// 先删除之前的7
				user.getNoChuCards().remove(card);
				user.getNoChuCards().remove(card);

				user.getNoChuCards().add(card);
				user.getNoChuCards().add(card);
				user.getDouble7s().add(card);
				send = true;
			}
		}

		// 南充吊追：一对gua天牌不能拆开打
		if (room.getRoomType() == ENRoomType.EN_ROOM_TYPE_NC_VALUE
				&& room.isDiaoZhui()) {
			Integer tpCount = holdMap.get(Integer.valueOf(66));
			Integer dPCount = holdMap.get(Integer.valueOf(11));
			Integer ftCount = holdMap.get(Integer.valueOf(56));
			Integer ddCount = holdMap.get(Integer.valueOf(12));

			user.getNoChuCards().remove(Integer.valueOf(66));
			user.getNoChuCards().remove(Integer.valueOf(66));
			user.getNoChuCards().remove(Integer.valueOf(11));
			user.getNoChuCards().remove(Integer.valueOf(11));
			user.getNoChuCards().remove(Integer.valueOf(56));
			user.getNoChuCards().remove(Integer.valueOf(56));
			user.getNoChuCards().remove(Integer.valueOf(12));
			user.getNoChuCards().remove(Integer.valueOf(12));

			if (tpCount != null && tpCount == 2 && dPCount == null) {
				user.getNoChuCards().add(Integer.valueOf(66));
				user.getNoChuCards().add(Integer.valueOf(66));

				user.getDoubleZhuiCards().add(Integer.valueOf(66));
				send = true;
			}
			if (dPCount != null && dPCount == 2 && tpCount == null) {
				user.getNoChuCards().add(Integer.valueOf(11));
				user.getNoChuCards().add(Integer.valueOf(11));

				user.getDoubleZhuiCards().add(Integer.valueOf(11));
				send = true;
			}
			if (ftCount != null && ftCount == 2 && ddCount == null) {
				user.getNoChuCards().add(Integer.valueOf(56));
				user.getNoChuCards().add(Integer.valueOf(56));

				user.getDoubleZhuiCards().add(Integer.valueOf(56));
				send = true;
			}
			if (ddCount != null && ddCount == 2 && ftCount == null) {
				user.getNoChuCards().add(Integer.valueOf(12));
				user.getNoChuCards().add(Integer.valueOf(12));

				user.getDoubleZhuiCards().add(Integer.valueOf(12));
				send = true;
			}
		}
		if (send && !isDealCard) {// 发牌先不发该通知，发完后再通知
			// 发送不能出牌的通知消息
			NotifyHandler.notifyDeathCardList(user);
		}
	}

	/**
	 * 报吃退后(即玩家点击吃退后)或吃牌后，去掉该死牌
	 * 
	 * @param card
	 * @param seat
	 */
	public static void removeDeathCard(int card, User user) {
		List<Integer> cards = user.getNoChuCards();
		boolean bool = false;
		for (int i = 0; i < cards.size(); i++) {
			if (card == cards.get(i)) {
				cards.remove(i);
				bool = true;
				break;
			}
		}
		if (bool) {
			user.setNoChuCards(cards);
			// 发送不能出牌的通知消息
			NotifyHandler.notifyDeathCardList(user);
		}
	}

	public static void removeDeathCardNCXC(int card, User user) {
		List<Integer> cards = user.getNoChuCards();
		boolean bool = false;
		for (int i = 0; i < cards.size(); i++) {
			if (card == cards.get(i)) {
				cards.remove(i);
				bool = true;
			}
		}
		if (bool) {
			user.setNoChuCards(cards);
			// 发送不能出牌的通知消息
			NotifyHandler.notifyDeathCardList(user);
		}
	}

	public static void addToDeathCard(int card, User user) {
		if (user.isFive()) {
			return;
		}
		List<Integer> cards = user.getNoChuCards();
		cards.add(card);
		// 发送不能出牌的通知消息
		NotifyHandler.notifyDeathCardList(user);
	}

	/**
	 * 吃啥，不能打啥，有一对不能扯
	 * 
	 * @param card
	 * @param seat
	 */
	public static void setDeathCardChi(Room room, User user, int card) {
		Map<Integer, Integer> tempCard = toMap(user.getHold());
		// 如果玩家手中有目标牌，则不能出，有两张，则既不能出，还不能再碰此目标牌
		// boolean change = false;
		if (tempCard.get(card) != null) {
			List<Integer> dcList = new ArrayList<Integer>();
			for (Integer dc : user.getNoChuCards()) {
				if (dc != card) {
					dcList.add(dc);
				}
			}
			for (int i = 0; i < tempCard.get(card); i++) {
				dcList.add(card);
			}
			user.setNoChuCards(dcList);
			// change = true;
		}
		Iterator<Integer> it = tempCard.keySet().iterator();
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			int count = tempCard.get(integer);
			if (integer == card && count == 2) {
				user.getNoCheCards().add(card);// 吃了不能扯
				// break;
			}
			if (room.isChiHongDaHei()) {// 吃红打黑:
				if (colorIsRed(card)) {
					if (getCardValue(card) == getCardValue(integer)
							&& colorIsRed(integer)) {
						for (int i = 0; i < count; i++) {
							user.getNoChuCards().remove(integer);
						}
						for (int i = 0; i < count; i++) {
							user.getNoChuCards().add(integer);
						}
						// change = true;
					}
				} else {
					if (getCardValue(card) == getCardValue(integer)) {
						for (int i = 0; i < count; i++) {
							user.getNoChuCards().remove(integer);
						}
						for (int i = 0; i < count; i++) {
							user.getNoChuCards().add(integer);
						}
						// change = true;
					}
				}
			}
		}
		// if (change) {
		// 发送该目标牌不能出的通知
		NotifyHandler.notifyDeathCardList(user);
		// }
	}

	/**
	 * 是否有人打过该牌了
	 * 
	 * @param room
	 * @param card
	 * @return
	 */
	public static boolean isHaveOfAllChuList(Room room, int card) {
		for (User user : room.getUsers().values()) {
			if (user.getChuListCards().contains(card)) {
				return true;
			}
		}
		return false;
	}

	public static List<Integer> getCanChiList(User user, int card) {
		List<Integer> chiList = new ArrayList<Integer>();
		for (int _card : allPais) {
			if (getCardValue(_card) + getCardValue(card) == 14) {
				chiList.add(_card);
			}
		}
		// 排除不能吃的牌
		if (!user.getNoChiCards().isEmpty()) {
			for (Integer integer : user.getNoChiCards()) {
				if (chiList.contains(integer)) {
					chiList.remove(integer);
				}
			}
		}
		return chiList;
	}

	public static void removeCardOfHold(User user, int card) {
		List<Integer> hold = user.getHold();
		List<Integer> holdTemp = new ArrayList<>();
		holdTemp.addAll(hold);
		for (Integer integer : hold) {
			if (integer == card) {
				holdTemp.remove(integer);
				break;
			}
		}
		user.setHold(holdTemp);
	}

}
