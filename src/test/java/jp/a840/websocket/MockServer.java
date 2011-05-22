package jp.a840.websocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import javax.net.ServerSocketFactory;

public class MockServer extends Thread {
	private List<Scenario> scenarioList = new ArrayList<MockServer.Scenario>();

	private int port;

	private Exchanger<Throwable> exchanger = new Exchanger<Throwable>();

	public MockServer(int port) {
		this.port = port;
		
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				try{
					exchanger.exchange(e);
				}catch(InterruptedException ie){
					ie.printStackTrace();
				}
			}
		});
	}
	
	public Throwable getThrowable(){
		try{
			return exchanger.exchange(null);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return null;
	}

	public void run() {
		try {
			ServerSocket serverSocket = ServerSocketFactory.getDefault()
					.createServerSocket(this.port);
			Socket socket = serverSocket.accept();
			for (Scenario scenario : scenarioList) {
				switch (scenario.getScenarioType()) {
				case READ:
					OutputStream os = socket.getOutputStream();
					os.write(scenario.getResponse());
					break;
				case WRITE:
					InputStream is = socket.getInputStream();
					byte[] buf = new byte[is.available()];
					is.read(buf);
					scenario.verifyRequest(buf);
					break;
				}
			}
			exchanger.exchange(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	enum ScenarioType {
		READ, WRITE;
	}

	public void addResponse(byte[] response) {
		Scenario scenario = new Scenario();
		scenario.setResponse(response);
		scenarioList.add(scenario);
	}

	public void addRequest(VerifyRequest verifyRequest) {
		Scenario scenario = new Scenario();
		scenario.setVerifyRequest(verifyRequest);
		scenarioList.add(scenario);
	}

	public interface VerifyRequest {
		public boolean verify(byte[] request);
	}
	
	public class Scenario {

		private byte[] response;
		private VerifyRequest verifyRequest;

		public byte[] getResponse() {
			return response;
		}

		public void setResponse(byte[] response) {
			this.response = response;
		}

		public void setVerifyRequest(VerifyRequest verifyRequest) {
			this.verifyRequest = verifyRequest;
		}

		public boolean verifyRequest(byte[] request) {
			return verifyRequest.verify(request);
		}

		public ScenarioType getScenarioType() {
			if (response == null) {
				return ScenarioType.WRITE;
			}
			return ScenarioType.READ;
		}
	}
}
