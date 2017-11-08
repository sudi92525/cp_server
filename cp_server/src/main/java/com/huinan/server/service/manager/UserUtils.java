package com.huinan.server.service.manager;

import java.util.ArrayList;
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
