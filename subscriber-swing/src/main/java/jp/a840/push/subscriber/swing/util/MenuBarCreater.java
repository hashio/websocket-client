/*
 * Created on 2005/10/01
 */
package jp.a840.push.subscriber.swing.util;

import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import jp.a840.push.subscriber.swing.SwingClient;

public class MenuBarCreater {
	public static JMenuBar createRealtimeMenuBar(ActionListener target) {
		JMenuBar mb = createCommonMenuBar();
		JMenuItem mi;

		JMenu cm = new JMenu("接続/切断");
		mi = new JMenuItem("接続");
		mi.setEnabled(false);
		mi.addActionListener(target);
		mi.setActionCommand("RealtimeConnect");
		cm.add(mi);
		mi = new JMenuItem("切断");
		mi.setEnabled(true);
		mi.addActionListener(target);
		mi.setActionCommand("RealtimeDisConnect");
		cm.add(mi);
		mi = new JMenuItem("接続先変更");
		mi.addActionListener(target);
		mi.setActionCommand("ChangeSource");
		cm.add(mi);
		mi = new JMenuItem("ヘルスチェック 間隔変更");
		mi.addActionListener(target);
		mi.setActionCommand("ChangeHealthCheckInterval");
		cm.add(mi);
		mi = new JMenuItem("再接続 間隔変更");
		mi.addActionListener(target);
		mi.setActionCommand("ChangeReconnectInterval");
		cm.add(mi);
		mb.add(cm, 0);

		return mb;
	}

	public static JMenuBar createHistoricalMenuBar(ActionListener target) {
		return createCommonMenuBar();
	}

	public static JMenuBar createCommonMenuBar() {
		ActionListener target = SwingClient.getInstance();

		JMenuItem mi;

		JMenuBar mb = new JMenuBar();

		JMenu dm = new JMenu("データ");
		mi = new JMenuItem("全データ削除");
		mi.addActionListener(target);
		mi.setActionCommand("ClearAllTableData");
		dm.add(mi);
		mb.add(dm);

		JMenu wm = new JMenu("ウィンドウ");

		JMenu rm = new JMenu("リアルタイム");
		mi = new JMenuItem("最良気配");
		mi.addActionListener(target);
		mi.setActionCommand("Realtime-BestRate");
		rm.add(mi);

		wm.add(rm);
		mb.add(wm);
		return mb;
	}
}
