package com.huinan.server.net.handler;

public class HeartBeatRec {
    private long maxDuration;
    private long minDuration;
    private int beatTimes;
    private long totalDuration;
    private long lastHeatBeatTime;
    private int unusualTimes; // 连续偏差的次数

    public HeartBeatRec() {
	maxDuration = 5000;
	minDuration = 5000;
	beatTimes = 1;
	totalDuration = 5000;
	lastHeatBeatTime = System.currentTimeMillis();
    }

    public long getMaxDuration() {
	return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
	this.maxDuration = maxDuration;
    }

    public long getMinDuration() {
	return minDuration;
    }

    public void setMinDuration(long minDuration) {
	this.minDuration = minDuration;
    }

    public int getBeatTimes() {
	return beatTimes;
    }

    public void increBeatTimes() {
	this.beatTimes++;
    }

    public long getTotalDuration() {
	return totalDuration;
    }

    public void increTotalDuration(long duration) {
	this.totalDuration += duration;
    }

    public void setLastHeatBeatTime(long lastHeatBeatTime) {
	this.lastHeatBeatTime = lastHeatBeatTime;
    }

    public long getLastHeatBeatTime() {
	return this.lastHeatBeatTime;
    }

    public int getUnusualTimes() {
	return unusualTimes;
    }

    public void setUnusualTimes(int unusualTimes) {
	this.unusualTimes = unusualTimes;
    }

    public void increUnusualTimes() {
	this.unusualTimes++;
    }
}
