package jp.a840.websocket.handler;

import jp.a840.websocket.WebSocket;
import jp.a840.websocket.exception.WebSocketException;
import jp.a840.websocket.frame.Frame;
import jp.a840.websocket.frame.draft06.TextFrame;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hasshie
 * Date: 11/10/30
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
abstract public class WebSocketTextHandler extends WebSocketHandlerAdapter {
    private Logger log = Logger.getLogger(WebSocketTextHandler.class.getName());

    @Override
    public void onMessage(WebSocket socket, Frame frame) {
        if(frame instanceof jp.a840.websocket.frame.draft06.TextFrame){
            jp.a840.websocket.frame.draft06.TextFrame textFrame = (jp.a840.websocket.frame.draft06.TextFrame)frame;
            onMessage(socket,textFrame.toString());
        } else if(frame instanceof jp.a840.websocket.frame.draft06.TextFrame){
            jp.a840.websocket.frame.draft76.TextFrame textFrame = (jp.a840.websocket.frame.draft76.TextFrame)frame;
            onMessage(socket,textFrame.toString());
        } else {
            log.warning("Recevied non text frame. frame type:" + frame.getHeader().getType());
        }
    }

    abstract public void onMessage(WebSocket socket, String text);
}
