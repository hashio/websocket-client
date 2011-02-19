package jp.a840.push.subscriber.swing.table.blink;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import jp.a840.push.subscriber.swing.table.SwingClientDefaultTableModel;


public class BlinkRow implements ActionListener {

	float updateCount = 9.0f;
	
	private float current = updateCount;
	
	private SwingClientDefaultTableModel tm;
	
	private int row;
	
	public void setRow(int row){
		this.row = row;
	}

	public int getRow(){
		return this.row;
	}
	
	public BlinkRow(SwingClientDefaultTableModel tm) {
		this.tm = tm;
	}

	public void init(){
		current = updateCount;
	}
	
	public float getBlink(){
		float f = current / updateCount;
		return f * f;
	}

	public void actionPerformed(ActionEvent ae) {		
		if(current > 0.0f){
			current--;
		}
		if(current <= 0.0f){
			Timer t = (Timer)ae.getSource();
			t.stop();
		}
		tm.fireTableRowsUpdated(row, row);
	}

}
