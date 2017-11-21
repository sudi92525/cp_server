package com.huinan.server.service.data;

import java.text.SimpleDateFormat;

/**
 *
 * renchao
 */
public class Constant {

	public static final int invalid = 0;// 无效
	public static final int normal = 1;// 正常
	public static final int keepFile = 2;// 归档

	public static final int cp_status_dating = 1;// 大厅
	public static final int cp_status_roomplay = 2;// 房间游戏
	public static final int cp_status_wait = 0;// 房间游戏
	public static final int cp_status_started = 1;// 房间游戏

	public static final int cp_dis_notagree = 0;// 不同意房间解散
	public static final int cp_dis_agree = 1;// 同意房间解散
	public static final int cp_dis_no = 2;// 未选择
	// 当玩家发起游戏解散申请后，若其他用户无操作，则默认180秒后解散游戏房间
	public static final int cp_dis_timeNum = 180;

	public static final String GAME_CODE = "xnsccp";

	public static final String time_format = "yyyy-MM-dd HH:mm:ss";
	public static final SimpleDateFormat sdf = new SimpleDateFormat(time_format);
	public static final int gameOver_small_type = 1;
	public static final int gameOver_big_type = 2;

	/** 房间人数4人 */
	public static final int PLAYER_NUM_FOUR = 4;
	/** 房间人数3人 */
	public static final int PLAYER_NUM_THREE = 3;

	/** 最小局数 */
	public static final int MIN_ROUND_NUM = 4;
	/**
	 * 庄家胡牌条件20坨
	 */
	public static final int hu_zj_score = 20;
	/**
	 * 闲家胡牌条件18坨
	 */
	public static final int hu_xj_score = 18;
	/**
	 * 尾家胡牌条件5坨
	 */
	public static final int hu_wj_score = 5;

	/**
	 * 偷4张红牌，10坨
	 */
	public static final int tou_4_hong = 10;
	/**
	 * 偷3张红牌，8坨
	 */
	public static final int tou_3_hong = 8;
	/**
	 * 偷4张黑牌，6坨
	 */
	public static final int tou_4_hei = 6;
	/**
	 * 偷3张黑牌，4坨
	 */
	public static final int tou_3_hei = 4;
	/**
	 * 碰红牌，8坨
	 */
	public static final int peng_hong = 8;
	/**
	 * 碰黑牌，4坨
	 */
	public static final int peng_hei = 4;
	/**
	 * 吃牌，普通计算，一张红牌算一坨
	 */
	public static final int chi_pt = 1;
	/**
	 * 0:状态不执行
	 */
	public static final int status_no = 0;
	/**
	 * 2:状态执行
	 */
	public static final int status_yes = 2;
	/**
	 * 临时目标牌，用户计算招牌动作时，当前目标牌在其他玩家出牌队列中的出牌次数
	 */
	public static final int temp_dest_card = 1010;
	/**
	 * 临时目标牌，用户计算招牌动作时，当前目标牌在其他玩家牌组合中出现次数
	 */
	public static final int temp_dest_card_col = 1011;

	public static final int TUO_XIANJIA_FAN = 30;
	public static final int TUO_ZHUANG_FAN = 32;

	public static final int TUO_XIAOJIA_1_FAN = 10;
	public static final int TUO_XIAOJIA_2_FAN = 12;
	public static final int TUO_XIAOJIA_3_FAN = 18;
	public static final int TUO_XIAOJIA_3_FAN_NC = 16;
	public static final int TUO_XIAOJIA_4_FAN_NC = 18;

	public static final int PLAYER_NUM = 4;

	/**
	 * 必碰的牌，临时缓存
	 */
	public static int bipeng_card = 0;
	/**
	 * 当玩家没有牌可以出时，需支付其他玩家1分
	 */
	public static int deathCard_score = 1;
}
