package jp.a840.push.subscriber.swing;

import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.naming.InitialContext;

public class SwingClientPreferences {
	private Preferences p;
	
	public static final String KEY_NAMING_FACTORY_INITIAL = InitialContext.INITIAL_CONTEXT_FACTORY;

	public static final String KEY_NAMING_PROVIDER_URL = InitialContext.PROVIDER_URL;

	public static final String KEY_WEBSOCKET_URL = "websocket.url";

	public static final String KEY_HEALTHCHECK_INTERVAL = "healthcheck.interval";
	
	public static final int DEFAULT_HEALTHCHECK_INTERVAL = 60000;

	public static final String KEY_RECONNECT_INTERVAL = "reconnect.interval";

	public static final String KEY_VIEW_CONVERT_PRODUCT_CODE = "view.convert.product.code";

	public SwingClientPreferences(){
		p = Preferences.userNodeForPackage(SwingClientPreferences.class);
		try{
			String factoryInitial = p.get(KEY_NAMING_FACTORY_INITIAL, null);
			if(factoryInitial == null){
				Properties jndi = ResourceLoader.getInstance().getProperties("jndi.properties");
				p.put(KEY_NAMING_FACTORY_INITIAL, jndi.getProperty(InitialContext.INITIAL_CONTEXT_FACTORY));
				p.put(KEY_NAMING_PROVIDER_URL, jndi.getProperty(InitialContext.PROVIDER_URL));
				p.putInt(KEY_HEALTHCHECK_INTERVAL, 60000);
				p.putInt(KEY_RECONNECT_INTERVAL, 10000);
				p.flush();
			}
			String websocketUrl = p.get(KEY_WEBSOCKET_URL, null);
			if(websocketUrl == null){
				Properties websocket = ResourceLoader.getInstance().getProperties("websocket.properties");
				p.put(KEY_WEBSOCKET_URL, websocket.getProperty(KEY_WEBSOCKET_URL));
				p.flush();
			}
		}catch(Exception e){
			throw new Error(e);
		}
	}
	
	public String get(String key){
		return p.get(key, null);
	}
	
	public int getInt(String key){
		return p.getInt(key, -1);
	}
	
	public long getLong(String key){
		return p.getLong(key, -1L);
	}
	
	public boolean getBoolean(String key){
		return p.getBoolean(key, false);
	}
	
	public float getFloat(String key){
		return p.getFloat(key, -1.0f);
	}
	
	public double getDouble(String key){
		return p.getDouble(key, -1.0d);
	}

	public byte[] getByteArray(String key){
		return p.getByteArray(key, null);
	}

	public void flush() throws BackingStoreException {
		p.flush();
	}

	public String get(String arg0, String arg1) {
		return p.get(arg0, arg1);
	}

	public boolean getBoolean(String arg0, boolean arg1) {
		return p.getBoolean(arg0, arg1);
	}

	public byte[] getByteArray(String arg0, byte[] arg1) {
		return p.getByteArray(arg0, arg1);
	}

	public double getDouble(String arg0, double arg1) {
		return p.getDouble(arg0, arg1);
	}

	public float getFloat(String arg0, float arg1) {
		return p.getFloat(arg0, arg1);
	}

	public int getInt(String arg0, int arg1) {
		return p.getInt(arg0, arg1);
	}

	public long getLong(String arg0, long arg1) {
		return p.getLong(arg0, arg1);
	}

	public void put(String arg0, String arg1) {
		p.put(arg0, arg1);
	}

	public void putBoolean(String arg0, boolean arg1) {
		p.putBoolean(arg0, arg1);
	}

	public void putByteArray(String arg0, byte[] arg1) {
		p.putByteArray(arg0, arg1);
	}

	public void putDouble(String arg0, double arg1) {
		p.putDouble(arg0, arg1);
	}

	public void putFloat(String arg0, float arg1) {
		p.putFloat(arg0, arg1);
	}

	public void putInt(String arg0, int arg1) {
		p.putInt(arg0, arg1);
	}

	public void putLong(String arg0, long arg1) {
		p.putLong(arg0, arg1);
	}

	public void remove(String arg0) {
		p.remove(arg0);
	}
	
}
