package jp.a840.push.subscriber.swing.table.blink;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import jp.a840.push.subscriber.swing.table.RowAttribute;
import jp.a840.push.subscriber.swing.table.SwingClientDefaultHashTableModel;


public class BlinkableRenderer implements TableCellRenderer {

	private float[] blinkHSB = Color.RGBtoHSB(0,255,0,null);

	private Color selectedColor = new Color(180, 215, 255);
	private float[] selectedBlinkHSB = Color.RGBtoHSB(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), null);

	
	private float blink = 0;

	// temporary buffer
	private float[] hsb = new float[3];
	private float[] tableHSB = new float[3];
	private float[] backgroundHSB = new float[3];

	private TableCellRenderer renderer;
	
	public BlinkableRenderer(TableCellRenderer renderer) {
		this.renderer = renderer;
	}

	public Component getTableCellRendererComponent(JTable table, Object data, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = renderer.getTableCellRendererComponent(table, data, isSelected, hasFocus, row, column);
		
		
		if(column == 0){
			SwingClientDefaultHashTableModel tm = (SwingClientDefaultHashTableModel)table.getModel();
			RowAttribute attr = tm.getAttribute(row);
			blink = attr.getBlinkRow().getBlink();
		}
		
		Color foregroundColor = null;
		Color backgroundColor = null;

		if (blink > 0) {
			if (isSelected) {
				hsb[0] = selectedBlinkHSB[0];
				hsb[1] = selectedBlinkHSB[1] + (1.0f - selectedBlinkHSB[1]) * blink;
				hsb[2] = selectedBlinkHSB[2];
			} else {
				Color base = table.getBackground();
				Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), tableHSB);
				hsb[0] = blinkHSB[0];
				hsb[1] = tableHSB[1] + (blinkHSB[1] - tableHSB[1]) * blink;
				hsb[2] = tableHSB[2] + (blinkHSB[2] - tableHSB[2]) * blink;
			}
			backgroundColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		} else {
			if (isSelected) {
				backgroundColor = selectedColor;
			} else {
				backgroundColor = table.getBackground();
			}
		}
		Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), backgroundHSB);
		if(backgroundHSB[2] < 0.5f){
			foregroundColor = Color.WHITE;
		}else{
			foregroundColor = Color.BLACK;				
		}

		c.setForeground(foregroundColor);
		c.setBackground(backgroundColor);
		return c;
	}

}
