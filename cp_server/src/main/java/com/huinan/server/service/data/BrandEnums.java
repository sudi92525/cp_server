package com.huinan.server.service.data;

public enum BrandEnums {
	dipai(11,1),		//地牌
	dingding(12,1),		//丁丁
	changer(22,0),		//长二
	hepai(13,1),		//和牌
	guaizi(23,0),		//拐子
	yaosi(14,1),		//幺四
	changsan(33,0),		//长三
	miaomiao(15,1),		//猫猫
	ersi(24,1),			//二四
	sansi(34,1),		//三四
	erwu(25,0),			//二五
	gaogao(16,1),		//高高
	sanwu(35,0),		//三五
	erliu(26,0),		//二六
	renpai(44,1),		//人牌
	hongjiu(45,1),		//红九
	waibian(36,0),		//弯鞭
	meizi(55,0),		//梅子
	siliu(46,1),		//四六
	futou(56,0),		//斧头
	tianpai(66,1);		//天牌
	
	public int code;
	public int num;
	
	private BrandEnums(int code,int num){
		this.code = code;
		this.num = num;
	}

	public static int getCodeNum(int code) {
		for(BrandEnums be:BrandEnums.values()){
			if(be.getCode() == code){
				return be.getNum();
			}
		}
		return 0;
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
}
