package jp.a840.push.subscriber.swing.table.renderer;

import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import jp.a840.push.subscriber.swing.ProductCodeMap;
import jp.a840.push.subscriber.swing.SwingClient;
import jp.a840.push.subscriber.swing.SwingClientPreferences;


public class DefaultProductCellRenderer extends JLabel implements TableCellRenderer {

	public DefaultProductCellRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table, Object data, boolean isSelected, boolean hasFocus, int row, int column) {
		this.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
		this.setHorizontalAlignment(JLabel.LEFT);
		setOpaque(true);

		if (isSelected) {
			this.setForeground(table.getSelectionForeground());
			this.setBackground(table.getSelectionBackground());
		} else {
			this.setForeground(table.getForeground());
			this.setBackground(table.getBackground());
		}

		String columnName = table.getColumnName(column);
		setFont(table.getFont());

		if (data == null) {
			this.setText("");
			return this;
		} else if (columnName.equals("ProductCode")) {
			if (SwingClient.getInstance().getPreferences().getBoolean(SwingClientPreferences.KEY_VIEW_CONVERT_PRODUCT_CODE)) {
				try {
					ProductCodeMap codeMap = ProductCodeMap.getInstance();
					String productName = codeMap.get((String) data);
					if (productName != null) {
						this.setText(productName);
					} else {
						this.setText(data.toString());
						this.setBackground(Color.RED);
						return this;
					}

				} catch (Exception e) {
					this.setText("Exception " + e.getMessage());
				}
			} else {
				this.setText(data.toString());
				return this;
			}
		} else {
			if (data instanceof BigDecimal) {
				this.setHorizontalAlignment(JLabel.RIGHT);
				this.setText(data.toString());
				return this;
			} else if (data instanceof Timestamp) {
				Timestamp ts = (Timestamp) data;
				this.setText(ts.toString());
				return this;
			} else {
				this.setText(data.toString());
				return this;
			}
		}
		return this;
	}

}
