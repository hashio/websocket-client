package jp.a840.push.subscriber.swing.panel.set;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableModel;

import jp.a840.push.subscriber.swing.panel.set.hash.HashTablePane;
import jp.a840.push.subscriber.swing.panel.set.list.ListTablePane;
import jp.a840.push.subscriber.swing.table.HashTableModel;
import jp.a840.push.subscriber.swing.table.ListTableModel;
import jp.a840.push.subscriber.swing.table.SwingClientTableModel;


public class SubscribePane extends JPanel {

	private int dividerLocation = 300;

	private boolean oneTouchExpandable = true;

	public SubscribePane(HashTableModel hashTableModel, ListTableModel listTableModel) {
		super();
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		HashTablePane top = new HashTablePane(hashTableModel);
		ListTablePane bottom = new ListTablePane(listTableModel);

//		this.addAncestorListener(top);
//		this.addAncestorListener(bottom);

		splitPane.setTopComponent(top);
		splitPane.setBottomComponent(bottom);
		splitPane.setOneTouchExpandable(oneTouchExpandable);
		splitPane.setDividerLocation(dividerLocation);

		JButton clearButton = new JButton();
		clearButton.setText("Clear");
		addClearActionListener(clearButton, top.getTable());
		addClearActionListener(clearButton, bottom.getTable());

		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		this.add(clearButton, BorderLayout.SOUTH);

	}

	private void addClearActionListener(JButton button, JTable t) {
		TableModel tm = t.getModel();
		if (tm instanceof SwingClientTableModel) {
			final SwingClientTableModel itm = (SwingClientTableModel) tm;
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					itm.clear();
				}
			});
		}
	}

}
