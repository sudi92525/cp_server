package com.huinan.server.service.manager;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.huinan.proto.CpMsgCs.CSNotifyActionFlow;
import com.huinan.proto.CpMsgCs.CSNotifyNextOperation;
import com.huinan.proto.CpMsgCs.CSNotifyPlayerDealCard;
import com.huinan.proto.CpMsgCs.CSResponsePlayBack;
import com.huinan.proto.CpMsgCs.ENActionType;
import com.huinan.proto.CpMsgCs.PBAction;
import com.huinan.proto.CpMsgCs.PBColumnInfo;
import com.huinan.server.service.data.User;

public class UserUtils {

	public static String groupNameEncode(String groupName) {
		byte[] byte1 = Base64.getEncoder().encode((groupName + "H").getBytes());
		String str1 = new String(byte1);
//		System.out.println("1:" + str1);

		byte[] byte2 = Base64.getEncoder().encode((str1 + "N").getBytes());
		String str2 = new String(byte2);
//		System.out.println("2:" + str2);

		byte[] byte3 = Base64.getEncoder().encode((str2 + "K").getBytes());
		String str3 = new String(byte3);
//		System.out.println("3:" + str3);

		byte[] byte4 = Base64.getEncoder().encode((str3 + "J").getBytes());
		String str4 = new String(byte4);
//		System.out.println("4:" + str4);
		return str4;
	}

	/**
	 * 二进制转为16进制字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	public static byte[] hex2byte(String hex) {
		byte[] ret = new byte[8];
		byte[] tmp = hex.getBytes();
		for (int i = 0; i < 8; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}

	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	/**
	 * 回放数据缓存 添加actionFlow
	 * 
	 * @param uBrand
	 * @param flow
	 */
	public static void setPlayBackData(User user,
			CSNotifyActionFlow.Builder flow) {
		PBAction action = flow.getAction();
		if (action.getActType() == ENActionType.EN_ACTION_TOU) {
			List<PBColumnInfo> cols = action.getColInfoList();
			if (cols != null && !cols.isEmpty()) {
				List<PBColumnInfo> newCols = new ArrayList<>();
				for (int i = 0; i < cols.size(); i++) {
					PBColumnInfo pbColumnInfo = cols.get(i);
					if (pbColumnInfo.getIsFan()) {
						PBColumnInfo.Builder build = PBColumnInfo.newBuilder();
						build.setScore(pbColumnInfo.getScore());
						build.addAllCards(pbColumnInfo.getCardsList());
						build.setColType(pbColumnInfo.getColType());
						build.setIsFan(false);
						build.setIsQishouTou(pbColumnInfo.getIsQishouTou());
						newCols.add(build.build());
					} else {
						newCols.add(pbColumnInfo);
					}
				}
				PBAction.Builder newAction = ProtoBuilder.buildPBAction(action);
				newAction.clearColInfo();
				newAction.addAllColInfo(newCols);
				flow.clearAction();
				flow.setAction(newAction.build());
			}
		}
		if (user.getPlayBack() == null) {
			CSResponsePlayBack.Builder playBack = CSResponsePlayBack
					.newBuilder();
			playBack.addFlows(flow);
			user.setPlayBack(playBack.build());
		} else {
			CSResponsePlayBack.Builder playBack = user.getPlayBack()
					.toBuilder();
			playBack.addFlows(flow);
			user.setPlayBack(playBack.build());
		}
	}

	/**
	 * 回放数据缓存 添加闹钟位置信息
	 * 
	 * @param uBrand
	 * @param next
	 */
	public static void setPlayBack_next(User user,
			CSNotifyNextOperation.Builder next) {
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		PBAction.Builder pb = PBAction.newBuilder();
		pb.setSeatIndex(next.getSeatIndex());
		pb.setLeftCardNum(next.getLeftCardNum());
		pb.setActType(ENActionType.EN_ACTION_NEXT);
		flow.setAction(pb);
		setPlayBackData(user, flow);
	}

	/**
	 * 回放数据缓存 添加系统发牌信息
	 * 
	 * @param uBrand
	 * @param deal
	 */
	public static void setPlayBack_deal(User user,
			CSNotifyPlayerDealCard.Builder deal) {
		CSNotifyActionFlow.Builder flow = CSNotifyActionFlow.newBuilder();
		PBAction.Builder pb = PBAction.newBuilder();
		pb.setSeatIndex(deal.getSeatIndex());
		pb.setDestCard(deal.getValue());
		pb.setIsFan(deal.getIsFanPai());
		pb.setLeftCardNum(deal.getLeftCardNum());
		pb.setTilesOnHandNum(deal.getTilesOnHandNum());
		pb.setActType(ENActionType.EN_ACTION_DEAL);
		flow.setAction(pb);
		setPlayBackData(user, flow);
	}

}
