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


public class GrizzlyConnectionChangePane extends JComponent {
	private static final Insets NORMAL_INSETS = new Insets(5, 2, 5, 2);

	private static final Insets LEFT_INSETS = new Insets(5, 7, 5, 2);

	private static final Insets RIGHT_INSETS = new Insets(5, 2, 5, 7);

	private GrizzlyConnectionChangeResult result = new GrizzlyConnectionChangeResult();

	private JButton cancelButton = new JButton("Cancel");
	
	private JButton okButton = new JButton("OK");
	
	private JTextField urlField = new JTextField();
		
	private GrizzlyConnectionChangePane(String url){
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
		
		result.setUrl(url);
	}
	
	public static GrizzlyConnectionChangeResult showDialog(Component parentComponent, String url){
		GrizzlyConnectionChangePane pane = new GrizzlyConnectionChangePane(url);
		
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

	public GrizzlyConnectionChangeResult getResult() {
		return result;
	}

	public static void main(String[] argv) {
		Properties p = new Properties();
		p.setProperty(Session.KEY_NAMING_PROVIDER_URL, "url");
		JFrame f = new JFrame();
		GrizzlyConnectionChangePane.showDialog(f, "foo");
	}
}
