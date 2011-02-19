package jp.a840.push.subscriber.swing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Session {
	public static final String KEY_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";

	public static final String KEY_NAMING_PROVIDER_URL = "java.naming.provider.url";

	public static final String KEY_HEALTHCHECK_INTERVAL = "healthcheck.interval";

	public static final String KEY_RECONNECT_INTERVAL = "reconnect.interval";

	public static final String KEY_VIEW_CONVERT_PRODUCT_CODE = "view.convert.product.code";

	private static File sessionFile = new File("session");

	private Properties p;

	public Session() {
	}

	public void load() throws Exception {
		if (sessionFile.canRead()) {
			FileInputStream fis = new FileInputStream(sessionFile);
			p = new Properties();
			p.load(fis);
			if (!p.containsKey(KEY_NAMING_PROVIDER_URL)) {
				createDefaultSession();
			}
			if (!p.containsKey(KEY_HEALTHCHECK_INTERVAL)) {
				p.setProperty(KEY_HEALTHCHECK_INTERVAL, "60000");
			}
			if (!p.containsKey(KEY_RECONNECT_INTERVAL)) {
				p.setProperty(KEY_RECONNECT_INTERVAL, "10000");
			}
			if (!p.containsKey(KEY_NAMING_FACTORY_INITIAL)) {
				p.setProperty(KEY_NAMING_FACTORY_INITIAL, "org.jnp.interfaces.NamingContextFactory");
			}
		} else {
			createDefaultSession();
		}
	}

	public String get(String key) {
		return p.getProperty(key);
	}

	public void set(String key, String value) {
		p.setProperty(key, value);
	}

	public void createDefaultSession() {
		try {
			Properties jndi = PropertiesLoader.getProperties("jndi.properties");
			copyFromProperties(jndi, KEY_NAMING_PROVIDER_URL);
			copyFromProperties(jndi, KEY_NAMING_FACTORY_INITIAL);
			p.setProperty(KEY_HEALTHCHECK_INTERVAL, "60000");
			p.setProperty(KEY_RECONNECT_INTERVAL, "10000");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void copyFromProperties(Properties pp, String key) {
		p.setProperty(key, pp.getProperty(key));
	}

	public void store() throws Exception {
		FileOutputStream fos = new FileOutputStream(sessionFile);
		p.store(fos, "TFX swing client session file");
	}

	public Properties getProperties() {
		return p;
	}
}
