package com.huinan.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import com.huinan.proto.CpMsgCs.PBColumnInfo;
import com.huinan.server.db.RedisDAO;
import com.huinan.server.service.data.Room;
import com.huinan.server.service.data.User;
import com.huinan.server.service.manager.CardManager;
import com.huinan.server.service.manager.ProtoBuilder;
import com.huinan.server.service.manager.RoomManager;

/**
 *
 * renchao
 */
public class NCTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFaPai() {
		Room room = new Room(123456, 1, true, true, true);
		for (int i = 0; i < 4; i++) {
			User user = new User(String.valueOf(i + 1));
			user.setSeatIndex(i + 1);
			if (i == 3) {
				user.setFive(true);
			}
			room.getUsers().put(i + 1, user);
		}
		RoomManager.shuffle(room);
		for (Integer card : room.getResetCards()) {
			// System.out.println("card:" + card);
		}
		System.out.println("card size=" + room.getResetCards().size());

		for (User user : room.getUsers().values()) {
			// System.out.println("user hold:");
			for (Integer card : user.getHold()) {
				// System.out.println("card:" + card);
			}
		}
		int fan = (int) Math.pow(2, 0);
		System.out.println("fen:" + fan);

		List<Integer> hh = new ArrayList<>();
		hh.add(3);
		hh.add(1);
		hh.add(1);

		System.out.println("size:" + hh.size());

		hh.remove(1);

		System.out.println("size:" + hh.size());

		hh.remove(Integer.valueOf(1));

		System.out.println("size:" + hh.size());
		System.out.println("size:" + hh.get(0));
	}

	public void testRed() {

		boolean isRed = CardManager.colorIsRed(12);
		System.out.println("red:" + isRed);

		System.out.println("true----------:" + isTrue());
	}

	private boolean isTrue() {
		for (int i = 0; i < 4; i++) {
			if (i == 2) {
				return true;
			}
		}

		return false;
	}

	public void testHu() {
		List<Integer> newHold = new ArrayList<>();
		// newHold.add(12);
		// newHold.add(56);
		// newHold.add(12);
		// newHold.add(56);
		newHold.add(16);
		newHold.add(16);
		newHold.add(16);

		// List<Integer> list7 = new ArrayList<>();
		// for (Integer integer : newHold) {
		// if (CardManager.getCardValue(integer) == 7) {
		// list7.add(integer);
		// }
		// }
		// newHold.removeAll(list7);
		System.out.println("newHold size=" + newHold.size());
		List<Integer> newHoldTemp = new ArrayList<>();
		newHoldTemp.addAll(newHold);

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

		// for (int i = 0; i < newHold.size(); i++) {
		// for (int j = 0; j < newHold.size(); j++) {
		// if ((CardManager.getCardValue(newHold.get(i)) + CardManager
		// .getCardValue(newHold.get(j))) == 14 && i != j) {
		// newHoldTemp.remove(newHold.get(j));
		// break;
		// }
		// }
		// }
		if (indexRecord.size() == newHold.size()) {
			System.out.println("hu la !!!!!!!!!");
		}
		System.out.println("indexRecord size=" + indexRecord.size());
	}

	public void testSameCards() {
		List<Integer> cards = CardManager.getSameCards(15);
		for (Integer integer : cards) {
			System.out.println("same card:" + integer);
		}
	}

	public void testRandomZhuang() {
		int lastZhuang = 1;
		// int seat2 = RoomManager.getLastSeat(lastZhuang);
		// 上把庄家的对门开始数
		// int seatDuiMen = RoomManager.getLastSeat(seat2);
		int seatDuiMen = 3;
		// 随机一张牌
		int index = new Random().nextInt(84) + 1;
		int zhuangCard = 13;// room.getResetCards().get(index);

		// 数这一局的庄家位置
		int cardValue = CardManager.getCardValue(zhuangCard);
		int yu = cardValue % 4;
		if (yu == 0) {
			yu = 4;
		}
		int thisSeat = seatDuiMen + yu - 1;
		if (thisSeat > 4) {
			thisSeat = thisSeat - 4;
		}
		System.out.println("thisSeat:" + thisSeat);
	}

	public void testDo() {
		List<Integer> newHold = new ArrayList<>();
		newHold.add(44);
		newHold.add(44);
		newHold.add(24);
		newHold.add(15);
		newHold.add(45);
		newHold.add(14);
		boolean dou14 = CardManager.checkDou14(newHold);
		System.out.println("dou14:" + dou14);

		PBColumnInfo.Builder col = PBColumnInfo.newBuilder();
		col.setIsFan(false);

		System.out.println("open1:" + col.getIsFan());
		PBColumnInfo info = col.build();
		User user = new User("13142");
		info = PBColumnInfo.newBuilder(
				ProtoBuilder.buildPBColumnInfo(user, info.getCardsList(),
						info.getColType(), true).build()).build();
		System.out.println("open2:" + info.getIsFan());

		int hh = 234565;
		String uuid = String.valueOf(hh);
		byte[] by = uuid.getBytes();
		String str = new String(by);
		int hh2 = Integer.valueOf(str);
		System.out.println("byte to int:" + hh2);
	}

	public void testDate() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(c.getTime());
		System.out.println("date string=" + dateStr);
	}

	public void testList() {
		List<Integer> list = new ArrayList<>();
		list.add(5);
		list.add(5);
		list.add(2);

		List<Integer> list2 = new ArrayList<>();
		list2.add(5);
		list2.add(2);

		// list.removeAll(list2);
		while (list.contains(5)) {
			list.remove(Integer.valueOf(5));
		}

		for (int i = 0; i < list.size(); i++) {
			// if (list.get(i) == 5) {
			// list.remove(i);
			// }
			// System.out.println("remove all :" + list.get(i));
		}
		System.out.println("list size :" + list.size());
	}
}
