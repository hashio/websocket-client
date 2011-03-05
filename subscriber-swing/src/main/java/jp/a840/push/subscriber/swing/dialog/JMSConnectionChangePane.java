package jp.a840.push.subscriber.swing.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jp.a840.push.subscriber.swing.Session;


public class JMSConnectionChangePane extends JComponent {
	private static final String[] providers = { "SwiftMQ", "ActiveMQ", "JBossJMS" };

	private static final String[] factories = { "com.swiftmq.jndi.InitialContextFactoryImpl", "org.apache.activemq.jndi.ActiveMQInitialContextFactory", "org.jnp.interfaces.NamingContextFactory" };

	private static final Insets NORMAL_INSETS = new Insets(5, 2, 5, 2);

	private static final Insets LEFT_INSETS = new Insets(5, 7, 5, 2);

	private static final Insets RIGHT_INSETS = new Insets(5, 2, 5, 7);

	private JMSConnectionChangeResult result = new JMSConnectionChangeResult();

	private JButton cancelButton = new JButton("Cancel");
	
	private JButton okButton = new JButton("OK");
	
	private JTextField urlField = new JTextField();
	
	private JComboBox comboBox = new JComboBox(providers);
	
	private JMSConnectionChangePane(String url, String factory){
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		Dimension d = new Dimension(300, 20);

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.anchor = GridBagConstraints.WEST;
		JLabel urlLabel = new JLabel("接続先", JLabel.LEFT);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = LEFT_INSETS;
		layout.setConstraints(urlLabel, gbc);
		this.add(urlLabel);

		urlField.setText(url);
		urlField.setPreferredSize(d);
		urlField.setMinimumSize(d);
		urlField.setCaretPosition(0);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = RIGHT_INSETS;
		layout.setConstraints(urlField, gbc);
		this.add(urlField);

		JLabel factoryInfoLabel = new JLabel("<html><p>接続先のJMSプロバイダが利用するファクトリを指定する必要があります。</p></html>", JLabel.LEFT);
		factoryInfoLabel.setPreferredSize(new Dimension(300, 50));
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = RIGHT_INSETS;
		layout.setConstraints(factoryInfoLabel, gbc);
		this.add(factoryInfoLabel);

		JLabel factoryLabel = new JLabel("プロバイダ", JLabel.LEFT);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = LEFT_INSETS;
		layout.setConstraints(factoryLabel, gbc);
		this.add(factoryLabel);

		int idx = Arrays.binarySearch(factories, factory);
		if (idx >= 0) {
			comboBox.setSelectedIndex(idx);
		}
		comboBox.setPreferredSize(d);
		comboBox.setMinimumSize(d);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = RIGHT_INSETS;
		layout.setConstraints(comboBox, gbc);
		this.add(comboBox);

		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = RIGHT_INSETS;
		
		JLabel versionInfoLabel = new JLabel("<html><p>JBossJMSの場合だけ旧バージョンを指定できます。</p></html>", JLabel.LEFT);
		versionInfoLabel.setPreferredSize(new Dimension(300, 50));
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = RIGHT_INSETS;
		layout.setConstraints(versionInfoLabel, gbc);
		this.add(versionInfoLabel);


		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0f;
		gbc.insets = NORMAL_INSETS;
		layout.setConstraints(cancelButton, gbc);
		this.add(cancelButton);

		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0f;
		gbc.insets = RIGHT_INSETS;
		layout.setConstraints(okButton, gbc);
		this.add(okButton);
		
		result.setFactory(factory);
		result.setUrl(url);
	}
	
	public static JMSConnectionChangeResult showDialog(Component parentComponent, String url, String factory){
		JMSConnectionChangePane pane = new JMSConnectionChangePane(url, factory);
		
		JDialog dialog = pane.createDialog(parentComponent);
		dialog.show();
		dialog.dispose();

		return pane.getResult();
	}
	
	private JDialog createDialog(Component parentComponent) {
		final JDialog dialog = createDialog(parentComponent, "接続先変更");
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parentComponent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				result.setCancel(true);
				dialog.dispose();
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				result.setUrl(urlField.getText());
				result.setFactory(factories[comboBox.getSelectedIndex()]);
				dialog.dispose();
			}
		});
		
		Container c = dialog.getContentPane();
		c.add(this);
		dialog.pack();
		return dialog;
	}

	private JDialog createDialog(Component parentComponent, String title){
		JDialog dialog;
		if(parentComponent instanceof Frame){
			dialog = new JDialog((Frame)parentComponent, title, true);
		}else{
			dialog = new JDialog((Dialog)parentComponent, title, true);
		}
		return dialog;
	}

	public JMSConnectionChangeResult getResult() {
		return result;
	}

	public static void main(String[] argv) {
		Properties p = new Properties();
		p.setProperty(Session.KEY_NAMING_FACTORY_INITIAL, "factory");
		p.setProperty(Session.KEY_NAMING_PROVIDER_URL, "url");
		JFrame f = new JFrame();
		JMSConnectionChangePane.showDialog(f, "foo", "boo");
	}
}
