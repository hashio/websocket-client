package jp.a840.push.subscriber.swing;

import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductCodeMap {

	private Logger log = LoggerFactory.getLogger(ProductCodeMap.class);

	private HashMap codeMap;

	private static ProductCodeMap instance;

	private ProductCodeMap() {
	}

	public static ProductCodeMap getInstance() throws Exception {
		if (instance == null) {
			synchronized (ProductCodeMap.class) {
				if (instance == null) {
					try {
						instance = new ProductCodeMap();
						instance.init();
						instance.load();
					} catch (Exception e) {
						e.printStackTrace();
						instance = null;
						throw e;
					}
				}
			}
		}
		return instance;
	}

	public void init() {
		codeMap = new HashMap();
	}

	synchronized public void load() throws FileNotFoundException {
		Properties p = PropertiesLoader.getProperties("product_code.map");
		Enumeration e = p.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = p.getProperty(key);
			/*
			try {
				value = new String(value.getBytes("ISO-8859-1"), "UTF-8");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			*/
			if (value != null) {
				if (log.isDebugEnabled()) {
					log.debug(key + ": " + value);
				}
				codeMap.put(key, value);
			} else {
				log.warn("code name is null: " + key);
			}
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return (String) codeMap.get(key);
	}
	/**
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return codeMap.containsKey(key);
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return codeMap.isEmpty();
	}

	/**
	 * @return
	 */
	public int size() {
		return codeMap.size();
	}

}
