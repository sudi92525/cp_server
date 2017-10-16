package com.huinan.server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.huinan.server.service.ActionMapper;
import com.huinan.server.service.IAction;
import com.huinan.server.service.manager.RoomManager;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public AppTest(String testName) {
	super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
	return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
	assertTrue(true);
    }

    public void testCreateRoom() {
	for (int i = 0; i < 1; i++) {
	    int id = RoomManager.getRoomCodeNumber();
	    System.out.println("room id:" + id);
	}
    }

    public void testGetAction() {
	IAction actor = ActionMapper.getActor(1);
	System.out.println(actor.getClass().getName());
    }

    public void testFaPai() {}

    public void testHu() {}
}
