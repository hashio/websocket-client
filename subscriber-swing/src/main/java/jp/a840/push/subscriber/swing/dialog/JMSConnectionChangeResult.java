package jp.a840.push.subscriber.swing.dialog;

public class JMSConnectionChangeResult {
	private boolean cancel = false;

	private String url;

	private String factory;
	
	public String getFactory() {
		return factory;
	}

	public void setFactory(String factory) {
		this.factory = factory;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}
	
}
