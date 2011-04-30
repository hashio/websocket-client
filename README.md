WebSocket Client
Copyright 2011 Takahiro Hashimoto

Support WebSocket Draft76, Draft06

Usage
=====

```java
WebSocket socket = WebSockets.createDraft06("ws://localhost:8080/ws/", new WebSocketHandler() {
    public void onOpen(WebSocket socket) {
         // TODO implement onOpen event
    }
    public void onMessage(WebSocket socket, Frame frame) {
         System.out.println(frame);
    }
    public void onError(WebSocket socket, WebSocketException e) {
         // TODO implement onError event
    }
    public void onClose(WebSocket socket) {
         // TODO implement onClose event
    }
}, null);

// connect
socket.connect();
...
socket.close();
```


        
#### Example: Sample of Jetty7 websocket chat servlet


System Property
===============

websocket.origin (Default:none)  
-------------------------------------------
use Origin header

websocket.bufferSize (Default:32767)  
---------------------------------------------------
received buffer size  
If you ever receive large frames to increase the buffer size

 websocket.queueSize (Default:500)  
------------------------------------------------
send queue size  
If you exceeded the size of the queue, an exception will be thrown

 websocket.packatdump  
--------------------------------
For debug  
Packet dump print to a console
    
  - ALL     ... dump All sending,receiving
  - HS_UP   ... dump Handshake sending
  - HS_DOWN ... dump Handshake receiving
  - FR_UP   ... dump Frame sending
  - FR_DOWN ... dump Frame receiving


####Example: All stream dump
    System.setProperty("websocket.packatdump", String.valueOf(
                                             PacketDumpStreamHandler.ALL
                                     ));

####Example: Not dump FR_DOWN    
    System.setProperty("websocket.packatdump", String.valueOf(
                                               PacketDumpStreamHandler.HS_UP
                                             | PacketDumpStreamHandler.HS_DOWN
                                             | PacketDumpStreamHandler.FR_UP
                                      ));
