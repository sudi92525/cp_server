package com.huinan.server.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.net.ClientRequest;

/**
 *
 * renchao
 */
public interface IAction {
	public static final Logger LOGGER = LogManager.getLogger();

	public void Action(ClientRequest request) throws Exception;
}
