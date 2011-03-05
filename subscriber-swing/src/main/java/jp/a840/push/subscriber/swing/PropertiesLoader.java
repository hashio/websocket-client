package jp.a840.push.subscriber.swing;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.discovery.jdk.JDKHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load properties file from system classpath
 */
public class PropertiesLoader {
	private static Logger log = LoggerFactory.getLogger(PropertiesLoader.class);

	public static InputStream getResourceAsStream(String propertyFileName) {
		JDKHooks hook = JDKHooks.getJDKHooks();
		ClassLoader[] classLoaders = {
				hook.getSystemClassLoader(),
				hook.getThreadContextClassLoader(),
				PropertiesLoader.class.getClassLoader()
		};
		
		for(int i = 0; i < classLoaders.length; i++){
			InputStream is = ResourceLoader.findResource(classLoaders[i], propertyFileName);
			if(is != null){
				log.info( "load properties: " + propertyFileName);
				return is;			
			}
		}
		return null;
	}
	
	public static Properties getProperties(String propertyFileName) {
		return getProperties(getResourceAsStream(propertyFileName));
	}

	public static Properties getProperties(InputStream is) {
		if(is == null){
			return null;
		}
		Properties p = new Properties();
		try{
			p.load(is);
		}catch(Exception e){
			;
		}
		return p;
	}
}

