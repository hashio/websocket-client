package jp.a840.push.subscriber.swing.table;

import javax.swing.Timer;

import jp.a840.push.subscriber.swing.table.blink.BlinkRow;


public class RowAttribute {
	private BlinkRow blinkRow;
	private Timer blinkTimer;
	private String key;
	
	public BlinkRow getBlinkRow() {
		return blinkRow;
	}
	public void setBlinkRow(BlinkRow blinkRow) {
		this.blinkRow = blinkRow;
	}
	public Timer getBlinkTimer() {
		return blinkTimer;
	}
	public void setBlinkTimer(Timer blinkTimer) {
		this.blinkTimer = blinkTimer;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}
