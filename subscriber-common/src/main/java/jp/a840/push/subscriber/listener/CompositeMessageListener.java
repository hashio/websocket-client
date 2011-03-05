package jp.a840.push.subscriber.listener;

import java.util.ArrayList;
import java.util.List;

import jp.a840.push.subscriber.event.MessageEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 複数のリスナーへプロキシするためのクラス.
 * 一部のリスナーからだけフィードしたデータをフィルタしたい時はこれを継承して実装してみてください. 
 * <br>
 *
 */
public class CompositeMessageListener implements MessageListener {
    private Logger log = LoggerFactory.getLogger(CompositeMessageListener.class);
    
    protected List messageListenerList = new ArrayList();
    protected MessageListener[] messageListeners = new MessageListener[0];
    
    public CompositeMessageListener() {
        super();
    }

    public void onMessage(MessageEvent m) {
    	MessageListener[] listeners = messageListeners;
        for(int i = 0; i < listeners.length; i++){
            if(log.isTraceEnabled()){
                log.trace("fire `onMessage' event to listener: " + listeners[i].toString());
            }
            listeners[i].onMessage(m);
        }
    }

    private void synchronizeMessageListenerList(){
        MessageListener[] listeners = new MessageListener[messageListenerList.size()];
        messageListenerList.toArray(listeners);
        this.messageListeners = listeners;
    }
    
    public void addMessageListener(MessageListener listener){
        if(listener == null){
            return;
        }
        if(log.isTraceEnabled()){
            log.trace("add  message listener: " + listener.toString());
        }
        synchronized (messageListenerList) {
        	messageListenerList.add(listener);
        }
        synchronizeMessageListenerList();
    }

    public void removeMessageListener(MessageListener listener){
        if(listener == null){
            return;
        }
        if(messageListenerList.size() == 0){
            log.warn("Can't removed. listeners list is empty");
            return;
        }
        if(log.isTraceEnabled()){
            log.trace("remove  message listener: " + listener.toString());
        }
        synchronized (messageListenerList) {
            if(!messageListenerList.remove(listener)){
                log.warn("Can't removed. listener not found in list");
            }
            if(messageListenerList.size() == 0){
                log.warn("removed last message listener of  client manager");
            }
        }
        synchronizeMessageListenerList();
    }

    public List getMessageListenerList() {
        return messageListenerList;
    }
}
