WebSocket Client
=================
Copyright 2011 Takahiro Hashimoto

MIT license

Support WebSocket Specification Draft76, Draft06

Purpose
-------
The purpose of a WebSocket Client is implement to portable,high-performance
and easy use the push communication in the internet for Java client 

These servers are tested.

- Grizzly2.0 + WebSocket(Draft76)
- Jetty7.4.0 + WebSocket(Draft76)
- Jetty7.4.0 + WebSocket(Draft06)


Requirements
-----------
- JDK5 or higher

Build And Install
=================

+ build with maven2

```shell
cd websocket-client
mvn clean install
```

+ add websocket-client dependency to the pom.xml of your application

```xml
<dependency>
  <groupId>jp.a840.websocket</groupId>
  <artifactId>websocket-client</artifactId>
  <version>0.8.0-SNAPSHOT</version>
</dependency>
```

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

TODO
====

- Support Proxy
- Send large frame with split to fragment frames
        
#### Example: Sample of Jetty7 websocket chat servlet

```java
WebSocket socket = WebSockets.createDraft06("ws://localhost:8080/ws/", new WebSocketHandler() {
    public void onOpen(WebSocket socket) {
        System.err.println("Open");
        try {
            socket.send(socket.createFrame(System.getenv("USER") + ":has joined!"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
        
    public void onMessage(WebSocket socket, Frame frame) {
        if(!frame.toString().startsWith(System.getenv("USER"))){
            try {
                socket.send(socket.createFrame(System.getenv("USER") + ":(echo)" + frame.toString()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.println(frame);
    }
        
    public void onError(WebSocket socket, WebSocketException e) {
        e.printStackTrace();
    }
        
    public void onClose(WebSocket socket) {
        System.err.println("Closed");
    }
}, "chat");
socket.connect();
socket.setBlockingMode(true);
```

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
