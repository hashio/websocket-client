package jp.a840.push.subscriber.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import jp.a840.push.subscriber.JMSSubscriber;
import jp.a840.push.subscriber.Subscriber;
import jp.a840.push.subscriber.event.ConnectionEvent;
import jp.a840.push.subscriber.event.ExceptionEvent;
import jp.a840.push.subscriber.grizzly.GrizzlySubscriber;
import jp.a840.push.subscriber.listener.ConnectionListener;
import jp.a840.push.subscriber.listener.ExceptionListener;
import jp.a840.push.subscriber.swing.dialog.GrizzlyConnectionChangePane;
import jp.a840.push.subscriber.swing.dialog.GrizzlyConnectionChangeResult;
import jp.a840.push.subscriber.swing.dialog.JMSConnectionChangePane;
import jp.a840.push.subscriber.swing.dialog.JMSConnectionChangeResult;
import jp.a840.push.subscriber.swing.listener.RealtimeTableModelManager;
import jp.a840.push.subscriber.swing.panel.lastupdate.hash.LastUpdatePane;
import jp.a840.push.subscriber.swing.panel.lastupdate.hash.RateLastUpdateTableModel;
import jp.a840.push.subscriber.swing.panel.set.RatePane;
import jp.a840.push.subscriber.swing.util.MenuBarCreater;
import jp.a840.push.subscriber.wsc.WSCSubscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * SwingClient
 * 
 * Subscriber GUI client for test
 */
public class SwingClient extends JFrame implements ActionListener, ConnectionListener {
	private static Log log = LogFactory.getLog(SwingClient.class);

	public static boolean quit = false;

	private Subscriber sub = null;

	private static SwingClient client;

	private SwingClientPreferences preferences;

	public static void main(String[] args) {
		try {

			// L&F
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (Exception e) {
			}

			/* create myself */
			client = SwingClient.getInstance();

			/* set terminate process handling */
			client.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					close();
				}
			});

			/* GUI window size */
			client.setBounds(0, 0, 640, 480);
			/* show client */
			client.setVisible(true);

			client.realtimeStart();
			log.info("Realtime Client Start.");
			while (true) {
				Thread.sleep(Long.MAX_VALUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SwingClient getInstance() {
		if (client == null) {
			synchronized (SwingClient.class) {
				if (client == null) {
					client = new SwingClient();
					client.init();
				}
			}
		}
		return client;
	}

	public static void close() {
		client.realtimeStop();
		System.exit(0);
	}

	public SwingClient() {
	}


	public void init() {
		preferences = new SwingClientPreferences();

//		sub = new JMSSubscriber();
//		sub.setNamingFactoryInitial( preferences.get(    SwingClientPreferences.KEY_NAMING_FACTORY_INITIAL));
//		sub.setNamingProviderUrl(    preferences.get(    SwingClientPreferences.KEY_NAMING_PROVIDER_URL));
//		sub.setHealthCheckInterval(  preferences.getInt( SwingClientPreferences.KEY_HEALTHCHECK_INTERVAL));
//		sub.setReconnectInterval(    preferences.getInt( SwingClientPreferences.KEY_RECONNECT_INTERVAL));
//		GrizzlySubscriber grizzlySubscriber = new GrizzlySubscriber();
//		grizzlySubscriber.setLocation(preferences.get(SwingClientPreferences.KEY_WEBSOCKET_URL));
//		grizzlySubscriber.setConnectionTimeout(10);
//		sub = grizzlySubscriber;
		WSCSubscriber wscSubscriber = new WSCSubscriber();
		wscSubscriber.setLocation(preferences.get(SwingClientPreferences.KEY_WEBSOCKET_URL));
		wscSubscriber.setConnectionTimeout(60);
		sub = wscSubscriber;

		sub.addConnectionListener(this);
		sub.addMessageListener(RealtimeTableModelManager.getInstance());
		sub.addExceptionListener(new ExceptionListener() {
			public void onException(ExceptionEvent e) {
				e.getException().printStackTrace();
			}
		});

//		sub.addSubscribe("tfxinfotopic", null);

		Container content = this.getContentPane();

		this.setJMenuBar(MenuBarCreater.createRealtimeMenuBar(this));
		JComponent component = new LastUpdatePane(new RateLastUpdateTableModel());

		content.add(component, BorderLayout.CENTER);
		/*
		 * JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP,
		 * JTabbedPane.SCROLL_TAB_LAYOUT); tab.addTab("JIJI",
		 * createRealtimeJIJI());
		 * 
		 * JTabbedPane t; t = new JTabbedPane(JTabbedPane.TOP,
		 * JTabbedPane.SCROLL_TAB_LAYOUT); tab.addTab("CNS",
		 * createRealtimeCNS());
		 * 
		 * t = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		 * tab.addTab("TOCOM", createRealtimeTOCOM());
		 * 
		 * t = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		 * tab.addTab("TELERATE", createRealtimeTELERATE());
		 */

		/*
		 * JPanel p1 = new JPanel(); p1.add(scrPane);
		 */
		/*
		 * content.add(tab, BorderLayout.CENTER);
		 */
	}

	public void realtimeStart() {
		if (!sub.isAlive()) {
			try {
				sub.start();
				changeMenuRealtimeConnect();
			} catch (Exception ex) {
				ex.printStackTrace();
				sub.stop();
				changeMenuRealtimeDisconnect();
			}
		}
	}

	public void changeMenuRealtimeConnect() {
		JMenuItem connectMenuItem = this.getJMenuBar().getMenu(0).getItem(0);
		JMenuItem disconnectMenuItem = this.getJMenuBar().getMenu(0).getItem(1);
		connectMenuItem.setEnabled(false);
		disconnectMenuItem.setEnabled(true);
	}

	public void changeMenuRealtimeDisconnect() {
		JMenuItem connectMenuItem = this.getJMenuBar().getMenu(0).getItem(0);
		JMenuItem disconnectMenuItem = this.getJMenuBar().getMenu(0).getItem(1);
		connectMenuItem.setEnabled(true);
		disconnectMenuItem.setEnabled(false);
		setTitle("Subscriber - disconnected");
	}

	public void realtimeStop() {
		JMenuItem connectMenuItem = this.getJMenuBar().getMenu(0).getItem(0);
		JMenuItem disconnectMenuItem = this.getJMenuBar().getMenu(0).getItem(1);
		if (sub.isAlive()) {
			sub.stop();
		}
		connectMenuItem.setEnabled(true);
		disconnectMenuItem.setEnabled(false);
		setTitle("Subscriber - disconnected");
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("RealtimeConnect")) {
			if (!sub.isAlive()) {
				realtimeStart();
			} else {
				JOptionPane.showMessageDialog(this, "Already connected.");
			}
		} else if (cmd.equals("ClearAllTableData")) {
			RealtimeTableModelManager.getInstance().clearAll();
		} else if (cmd.equals("RealtimeDisConnect")) {
			realtimeStop();
		} else if (cmd.equals("ChangeSource")) {
			changeSource();
		} else if (cmd.equals("ChangeHealthCheckInterval")) {
			changeHealthCheckInterval();
		} else if (cmd.equals("ChangeReconnectInterval")) {
			changeReconnectInterval();
		} else if (cmd.equals("ShowProductCode")) {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) e.getSource();
			preferences.putBoolean(Session.KEY_VIEW_CONVERT_PRODUCT_CODE, !mi.isSelected());
			try{
				preferences.flush();
			}catch(BackingStoreException bse){
				bse.printStackTrace();
			}
		} else if (cmd.equals("Realtime-BestRate")) {
			JFrame frame = new JFrame("Realtime - rate");
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setJMenuBar(MenuBarCreater.createRealtimeMenuBar(this));
			frame.setBounds(client.getBounds());
			JComponent p = new RatePane();
			frame.getContentPane().add(p);
			frame.setVisible(true);
		}
	}

	private void changeSource() {
		if(sub instanceof JMSSubscriber){
			JMSSubscriber jmsSubscriber = (JMSSubscriber)sub;
			JMSConnectionChangeResult result = JMSConnectionChangePane
					.showDialog(
							this,
							preferences
									.get(SwingClientPreferences.KEY_NAMING_PROVIDER_URL),
							preferences
									.get(SwingClientPreferences.KEY_NAMING_FACTORY_INITIAL));
			if (result.isCancel()) {
				return;
			}
			try {
				realtimeStop();
				jmsSubscriber.setNamingFactoryInitial(result.getFactory());
				jmsSubscriber.setNamingProviderUrl(result.getUrl());
				jmsSubscriber.init();
				realtimeStart();
				preferences.put(SwingClientPreferences.KEY_NAMING_PROVIDER_URL,
						result.getUrl());
				preferences.put(
						SwingClientPreferences.KEY_NAMING_FACTORY_INITIAL,
						result.getFactory());
				preferences.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}else if(sub instanceof GrizzlySubscriber){
			GrizzlySubscriber grizzlySubscriber = (GrizzlySubscriber)sub;
			GrizzlyConnectionChangeResult result = GrizzlyConnectionChangePane
			.showDialog(
					this,
					preferences
							.get(SwingClientPreferences.KEY_WEBSOCKET_URL));
			if (result.isCancel()) {
				return;
			}
			try {
				realtimeStop();
				grizzlySubscriber.setLocation(result.getUrl());
				grizzlySubscriber.init();
				realtimeStart();
				preferences.put(SwingClientPreferences.KEY_WEBSOCKET_URL,
						result.getUrl());
				preferences.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void changeHealthCheckInterval() {
		String defaultHealthCheckInterval = String.valueOf(preferences.getInt(SwingClientPreferences.KEY_HEALTHCHECK_INTERVAL));
		String healthCheckIntervalStr = JOptionPane.showInputDialog(this, "health check interval:(ms)", defaultHealthCheckInterval);
		if (healthCheckIntervalStr == null) {
			healthCheckIntervalStr = defaultHealthCheckInterval;
		}
		
		int healthCheckInterval = Integer.valueOf(healthCheckIntervalStr).intValue();
		
		try {
			realtimeStop();
			sub.setHealthCheckInterval(healthCheckInterval);
			sub.init();
			realtimeStart();
			preferences.putInt(SwingClientPreferences.KEY_HEALTHCHECK_INTERVAL, healthCheckInterval);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void changeReconnectInterval() {
		String defaultReconnectInterval = String.valueOf(preferences.getInt(SwingClientPreferences.KEY_RECONNECT_INTERVAL));
		String reconnectIntervalStr = JOptionPane.showInputDialog(this, "reconnect interval:(ms)", defaultReconnectInterval);
		if (reconnectIntervalStr == null) {
			reconnectIntervalStr = defaultReconnectInterval;
		}
		
		int reconnectInterval = Integer.valueOf(reconnectIntervalStr).intValue();
		
		try {
			realtimeStop();
			sub.setReconnectInterval(reconnectInterval);
			sub.init();
			realtimeStart();
			preferences.putInt(SwingClientPreferences.KEY_RECONNECT_INTERVAL, reconnectInterval);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onConnected(ConnectionEvent e) {
		setTitle("Subscriber - connected");
		changeMenuRealtimeConnect();
	}

	public void onDisonnected(ConnectionEvent e) {
		setTitle("Subscriber - disconnected");
		changeMenuRealtimeDisconnect();
	}

	public void onRevivaled(ConnectionEvent e) {
		setTitle("Subscriber - revival");
		changeMenuRealtimeConnect();
	}

	public SwingClientPreferences getPreferences() {
		return preferences;
	}
}
