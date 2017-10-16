package com.huinan.server.service;

import java.util.Map;

public interface ICallBack<T> {

	void callBack(Map<String, Object> params, T t);
}
