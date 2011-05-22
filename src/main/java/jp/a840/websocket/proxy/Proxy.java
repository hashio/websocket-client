package jp.a840.websocket.proxy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.a840.websocket.WebSocketException;
import jp.a840.websocket.handshake.ProxyHandshake;

public class Proxy {
	/** The log. */
	private static Logger log = Logger.getLogger(Proxy.class.getName());

	private boolean autoDetect = false;

	private InetSocketAddress proxy;
	
	private ProxyCredentials credentials;
	
	public Proxy(){
		this.autoDetect = true;
		this.credentials = null;
	}
	
	public Proxy(String userName, String password){
		this.autoDetect = true;
		this.credentials = new ProxyBasicCredentials(null, userName, password);
	}
	
	public Proxy(ProxyCredentials credentials){
		this.autoDetect = true;
		this.credentials = credentials;
	}
	
	public Proxy(InetSocketAddress proxyAddress, String userName, String password){
		this.proxy = proxyAddress;
		this.credentials = new ProxyBasicCredentials(null, userName, password);
	}
	
	public Proxy(InetSocketAddress proxyAddress, ProxyCredentials credentials){
		this.proxy = proxyAddress;
		this.credentials = credentials;
	}
	
	protected InetSocketAddress findProxy(InetSocketAddress endpoint) throws WebSocketException {
		try{
			System.setProperty("java.net.useSystemProxies","true");
			URI proxyUri = new URI("http", null, endpoint.getHostName(), endpoint.getPort(), null,null,null);
			List<java.net.Proxy> proxyList = ProxySelector.getDefault().select(proxyUri);
			if(proxyList != null && proxyList.size() > 0 && !proxyList.get(0).type().equals(java.net.Proxy.Type.DIRECT)){
				if (log.isLoggable(Level.FINER)) {
					int i = 1;
					for (java.net.Proxy p : proxyList) {
						log.finer("Proxy[" + i++ + "]" + p.toString());
					}
				}
				InetSocketAddress address = (InetSocketAddress)proxyList.get(0).address();
				try{
					// FIXME A proxy(ip address only) of the ProxySelector has the Unresolved address.
					// so this code do resolve an ip address proxy
					address = new InetSocketAddress(InetAddress.getByName(address.getHostName()), address.getPort());
				}catch(UnknownHostException e){
					e.printStackTrace();
				}
				return address;
			}
		}catch(URISyntaxException e){
			throw new WebSocketException(3032, e);
		}
		return null;
	}

	public ProxyHandshake getProxyHandshake(InetSocketAddress endpoint) throws WebSocketException {
		InetSocketAddress proxy = null;
		if(autoDetect){
			proxy = findProxy(endpoint);
			if(proxy == null){
				return null;
			}
		}else{
			proxy = this.proxy;
		}
		ProxyHandshake proxyHandshake = new ProxyHandshake(proxy, endpoint, this.credentials);
		return proxyHandshake;
	}
}
