/*
 * The MIT License
 * 
 * Copyright (c) 2011 Takahiro Hashimoto
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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


/**
 * The Class Proxy.
 *
 * @author Takahiro Hashimoto
 */
public class Proxy {
	/** The log. */
	private static Logger log = Logger.getLogger(Proxy.class.getName());

	/** The auto detect. */
	private boolean autoDetect = false;

	/** The proxy. */
	private InetSocketAddress proxy;
	
	/** The credentials. */
	private ProxyCredentials credentials;
	
	/**
	 * Instantiates a new proxy.
	 */
	public Proxy(){
		this.autoDetect = true;
		this.credentials = null;
	}
	
	/**
	 * Instantiates a new proxy.
	 *
	 * @param userName the user name
	 * @param password the password
	 */
	public Proxy(String userName, String password){
		this.autoDetect = true;
		this.credentials = new ProxyBasicCredentials(null, userName, password);
	}
	
	/**
	 * Instantiates a new proxy.
	 *
	 * @param credentials the credentials
	 */
	public Proxy(ProxyCredentials credentials){
		this.autoDetect = true;
		this.credentials = credentials;
	}
	
	/**
	 * Instantiates a new proxy.
	 *
	 * @param proxyAddress the proxy address
	 * @param userName the user name
	 * @param password the password
	 */
	public Proxy(InetSocketAddress proxyAddress, String userName, String password){
		this.proxy = proxyAddress;
		this.credentials = new ProxyBasicCredentials(null, userName, password);
	}
	
	/**
	 * Instantiates a new proxy.
	 *
	 * @param proxyAddress the proxy address
	 * @param credentials the credentials
	 */
	public Proxy(InetSocketAddress proxyAddress, ProxyCredentials credentials){
		this.proxy = proxyAddress;
		this.credentials = credentials;
	}
	
	/**
	 * Find proxy.
	 *
	 * @param endpoint the endpoint
	 * @return the inet socket address
	 * @throws WebSocketException the web socket exception
	 */
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

	/**
	 * Gets the proxy handshake.
	 *
	 * @param endpoint the endpoint
	 * @return the proxy handshake
	 * @throws WebSocketException the web socket exception
	 */
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
