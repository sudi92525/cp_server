package com.huinan.server.service.action.luanch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.huinan.proto.CpMsg.CpHead;
import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.proto.CpMsgCs.CSRequestInit;
import com.huinan.proto.CpMsgCs.CSResponseInit;
import com.huinan.proto.CpMsgCs.ENMessageError;
import com.huinan.server.net.ClientRequest;
import com.huinan.server.server.net.config.ServerConfig;
import com.huinan.server.service.AbsAction;

public class LuanchInit extends AbsAction {

	@Override
	public void Action(ClientRequest request) throws Exception {
		CSRequestInit requestBody = request.getMsg().getCsRequestInit();
		String requestJson = requestBody.getInitRequestJson();

		String responseJson = adminRequest(requestJson, ServerConfig
				.getInstance().getAdminInitUrl());

		CpMsgData.Builder msg = CpMsgData.newBuilder();
		CSResponseInit.Builder response = CSResponseInit.newBuilder();
		if (responseJson.isEmpty()) {
			response.setResult(ENMessageError.RESPONSE_FAIL);
		} else {
			response.setResult(ENMessageError.RESPONSE_SUCCESS);
		}
		response.setInitReaponseJson(responseJson);
		msg.setCsResponseInit(response);

		request.getClient().sendMessage(
				CpMsgData.CS_RESPONSE_INIT_FIELD_NUMBER, "",
				(CpHead) request.getHeadLite(), msg.build());
	}

	public static String adminRequest(String requestJson, String url) {
		StringBuffer result = new StringBuffer();
		InputStream is = null;
		InputStreamReader read = null;
		BufferedReader reader = null;
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection http = (HttpURLConnection) httpUrl
					.openConnection();
			// http.setRequestProperty("opcode", String.valueOf(opcode)); // 头部
			// http.setRequestProperty("appkey", appkey); // 头部
			// http.setRequestProperty("sign", sign); // 头部
			// http.setRequestProperty("tag", String.valueOf(tag)); // 头部
			// http.setRequestProperty("channelId", String.valueOf(channelId));
			// // 头部
			http.setConnectTimeout(5000);
			http.setReadTimeout(5000);
			http.setRequestMethod("POST"); // post方式
			http.setDoInput(true);
			http.setDoOutput(true);
			PrintWriter out = new PrintWriter(http.getOutputStream());
			out.print(requestJson); // 注意data传递方式
			out.flush();
			is = http.getInputStream();
			read = new InputStreamReader(is, "UTF-8");
			reader = new BufferedReader(read);
			String code = "";
			while ((code = reader.readLine()) != null) {
				result.append(code);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (read != null) {
					read.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

}
