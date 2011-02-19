package jp.a840.push.subscriber.listener;

import java.util.ArrayList;
import java.util.List;

import jp.a840.push.subscriber.event.ExceptionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 複数のリスナーへプロキシするためのクラス.
 * 一部のリスナーからだけフィードしたデータをフィルタしたい時はこれを継承して実装してみてください.
 * <br>
 *
 */
public class CompositeExceptionListener implements ExceptionListener {
    private Logger log = LoggerFactory.getLogger(CompositeExceptionListener.class);
    
    protected ExceptionListener[] exceptionListeners = new ExceptionListener[0];
    private List exceptionListenerList = new ArrayList();
    
    public CompositeExceptionListener() {
        super();
    }

    public void onException(ExceptionEvent e) {
    	ExceptionListener[] listners = exceptionListeners;
        for(int i = 0; i < listners.length; i++){
            if(log.isTraceEnabled()){
                log.trace("fire `onException' event to listener: " + listners[i].toString());
            }
            listners[i].onException(e);
        }
    }

    private void synchronizeExceptionListenerList(){
        ExceptionListener[] listeners = new ExceptionListener[exceptionListenerList.size()];
        exceptionListenerList.toArray(listeners);
        this.exceptionListeners = listeners;
    }
    
    public void addExceptionListener(ExceptionListener listener){
        if(listener == null){
            return;
        }
        if(log.isTraceEnabled()){
            log.trace("add exception listener: " + listener.toString());
        }
        synchronized (exceptionListenerList) {
            exceptionListenerList.add(listener);
        }
        synchronizeExceptionListenerList();
    }

    public void removeExceptionListener(ExceptionListener listener){
        if(listener == null){
            return;
        }
        if(exceptionListenerList.size() == 0){
            log.warn("Can't removed. listeners list is empty");
            return;
        }
        if(log.isTraceEnabled()){
            log.trace("remove exception listener: " + listener.toString());
        }
        synchronized (exceptionListenerList) {
            if(!exceptionListenerList.remove(listener)){
                log.warn("Can't removed. listener not found in list");
            }
            if(exceptionListenerList.size() == 0){
                log.warn("removed last message listener");
            }
        }
        synchronizeExceptionListenerList();
    }

    public List getExceptionListenerList() {
        return exceptionListenerList;
    }

}
