package jp.a840.push.subscriber.swing;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import org.apache.commons.discovery.Resource;
import org.apache.commons.discovery.ResourceIterator;
import org.apache.commons.discovery.jdk.JDKHooks;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.DiscoverResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Jar等のアーカイブ内のリソースを取得する
 * 
 * Created: Thu Oct 12 16:56:15 2006
 * 
 * @author t-hashimoto
 * @version 1.0
 */
public class ResourceLoader {
	private static Log log = LogFactory.getLog(ResourceLoader.class);

	private static ResourceLoader instance;

	protected ResourceLoader() {
	}

	public static ResourceLoader getInstance() {
		if (instance == null) {
			instance = new ResourceLoader();
		}
		return instance;
	}

	/**
	 * Jarのクラスローダを取得する
	 * 
	 * @param resourceName
	 *            リソース名
	 */
	public static ClassLoader getJarClassLoader(String target) throws Exception {
		if (!target.matches(".*¥¥..ar$")) {
			target = target + ".jar";
		}
		JDKHooks hook = JDKHooks.getJDKHooks();
		DiscoverResources discover = new DiscoverResources();
		ClassLoader systemClassLoader = hook.getSystemClassLoader();
		discover.addClassLoader(systemClassLoader);

		ResourceIterator ri = discover.findResources(target);
		Resource resource = null;
		while (ri.hasNext()) {
			resource = ri.nextResource();
			log.info("load jar: " + resource.getResource());
			URL[] urls = new URL[] { new URL("jar:" + resource.getResource()
					+ "!/") };
			return new URLClassLoader(urls, systemClassLoader);
		}
		return null;
	}

	/**
	 * 指定されたディレクトリにあるJarを全て含めたクラスローダを返す
	 * 
	 * @param directory
	 * @return
	 * @throws Exception
	 */
	public static ClassLoaders getJarClassLoadersFromDirectory(String directory)
			throws Exception {
		File f = new File(directory);
		if (!f.exists()) {
			log.warn(f + " is not found");
			return null;
		}
		if (!f.isDirectory()) {
			log.warn(f + " is not directory");
			return null;
		}

		FileFilter ff = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return false;
				if (f.getName().matches(".*¥¥.jar$"))
					return true;
				return false;
			}
		};

		JDKHooks hook = JDKHooks.getJDKHooks();
		ClassLoader systemClassLoader = hook.getSystemClassLoader();

		ClassLoaders classLoaders = new ClassLoaders();
		File[] files = f.listFiles(ff);
		for (int i = 0; i < files.length; i++) {
			String path = files[i].getCanonicalPath();
			log.info("load jar: " + path);
			URL[] urls = new URL[1];
			urls[0] = new URL("jar:file:" + path + "!/");
			classLoaders.put(new URLClassLoader(urls, systemClassLoader));
		}
		return classLoaders;
	}

	public static ClassLoader getSystemClassLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	/**
	 * ResourceLoaderのクラスローダからリソースを取得
	 * 
	 * @param resourceName
	 *            リソース名
	 */
	public InputStream findResource(String resourceName) {
		return findResource(this.getClass(), resourceName);
	}

	/**
	 * 渡されたオブジェクトのクラスローダからリソースを取得
	 * 
	 * @param obj
	 *            オブジェクト
	 * @param resourceName
	 *            リソース名
	 */
	public static InputStream findResource(Object obj, String resourceName) {
		return findResource(obj.getClass(), resourceName);
	}

	/**
	 * 渡されたクラスのクラスローダからリソースを取得
	 * 
	 * @param class クラス
	 * @param resourceName
	 *            リソース名
	 */
	public static InputStream findResource(Class clazz, String resourceName) {
		return findResource(clazz.getClassLoader(), resourceName);
	}

	/**
	 * 渡されたクラスローダからリソースを取得
	 * 
	 * @param classLoader
	 *            クラスローダ
	 * @param resourceName
	 *            リソース名
	 */
	public static InputStream findResource(ClassLoader classLoader,
			String resourceName) {
		DiscoverResources discover = new DiscoverResources();
		discover.addClassLoader(classLoader);

		ResourceIterator ri = discover.findResources(resourceName);
		while (ri.hasNext()) {
			Resource resource = ri.nextResource();
			log.debug("load resource: " + resource.getResource());
			return resource.getResourceAsStream();
		}
		return null;
	}

	public ResourceIterator findResources(String resourceName) {
		return findResources(this.getClass(), resourceName);
	}

	public static ResourceIterator findResources(Object obj, String resourceName) {
		return findResources(obj.getClass(), resourceName);
	}

	public static ResourceIterator findResources(Class clazz,
			String resourceName) {
		return findResources(clazz.getClassLoader(), resourceName);
	}

	public static ResourceIterator findResources(ClassLoader classLoader,
			String resourceName) {
		DiscoverResources discover = new DiscoverResources();
		discover.addClassLoader(classLoader);

		return discover.findResources(resourceName);
	}

	public InputStream findJarResource(String resourceName) {
		return findJarResource(this.getClass(), resourceName, false);
	}

	public static InputStream findJarResource(Object obj, String resourceName) {
		return findJarResource(obj.getClass(), resourceName, false);
	}

	public static InputStream findJarResource(Class clazz, String resourceName) {
		return findJarResource(clazz, resourceName, false);
	}

	public static InputStream findJarResource(ClassLoader classLoader,
			String resourceName) {
		return findJarResource(classLoader, resourceName, false);
	}

	public InputStream findJarResource(String resourceName, boolean onlyWebInf) {
		return findJarResource(this, resourceName, onlyWebInf);
	}

	public InputStream findJarResource(Object obj, String resourceName,
			boolean onlyWebInf) {
		return findJarResource(obj.getClass(), resourceName, onlyWebInf);
	}

	public static InputStream findJarResource(Class clazz, String resourceName,
			boolean onlyWebInf) {
		return findJarResource(clazz.getClassLoader(), resourceName, onlyWebInf);
	}

	public static InputStream findJarResource(ClassLoader classLoader,
			String resourceName, boolean onlyWebInf) {
		String prefix = "META-INF";
		InputStream is = null;
		if (resourceName.indexOf("/") != 0) {
			is = findResource(classLoader, prefix + "/" + resourceName);
		} else {
			is = findResource(classLoader, prefix + resourceName);
		}
		if (is == null && !onlyWebInf) {
			is = findResource(classLoader, resourceName);
		}
		return is;
	}

	public Properties getProperties(String propertiesName) throws IOException {
		return PropertiesLoader.getProperties(findResource(propertiesName));
	}
}
