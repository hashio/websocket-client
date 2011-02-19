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

import jp.a840.push.subscriber.swing.panel.set.hash.BestRateHashTablePane;
import jp.a840.push.subscriber.swing.panel.set.list.BestRateListTablePane;
import jp.a840.push.subscriber.swing.table.SwingClientTableModel;


public class BestRatePane extends JPanel {

	private int dividerLocation = 300;

	private boolean oneTouchExpandable = true;

	public BestRatePane() {
		super();
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		BestRateHashTablePane top = new BestRateHashTablePane();
		BestRateListTablePane bottom = new BestRateListTablePane();

		this.addAncestorListener(top);
		this.addAncestorListener(bottom);

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

	private void applyListener(Object obj) {
		if (obj instanceof AncestorListener) {
			this.addAncestorListener((AncestorListener) obj);
		}
		if (obj instanceof ComponentListener) {
			this.addComponentListener((ComponentListener) obj);
		}
		if (obj instanceof ContainerListener) {
			this.addContainerListener((ContainerListener) obj);
		}
		if (obj instanceof FocusListener) {
			this.addFocusListener((FocusListener) obj);
		}
		if (obj instanceof FocusListener) {
			this.addFocusListener((FocusListener) obj);
		}
	}

}
