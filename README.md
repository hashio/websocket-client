WebSocket Client
=================
Copyright 2011,2012 Takahiro Hashimoto

MIT license

Purpose
-------
The purpose of a WebSocket Client is implement to portable,high-performance
and easy use the push communication in the internet for Java client 

Support
-------

- JDK5 or higher
- WebSocket Specification Draft76, Draft06, RFC6455
- SSL/TLS with wss://
- proxy [Basic,Digest,Negotiate(Windows only) authentication support]


Test running for these combinations.

- [Draft76] Grizzly2.0   + WebSocket
- [Draft06] Grizzly2.1.1 + WebSocket
- [Draft76] Jetty7.4.0   + WebSocket
- [Draft06] Jetty7.4.0   + WebSocket
- [Draft06 + Proxy] Apache(mod_proxy) + Jetty7.4.0 + WebSocket
- [RFC6455] netty + WebSocket
- [RFC6455 + Proxy] www.websocket.org/echo + WebSocket


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

#### Example: Sample of Jetty7 websocket chat servlet

```java
WebSocket socket = WebSockets.createDraft06("ws://localhost:8080/ws/", new WebSocketHandler() {
    public void onOpen(WebSocket socket) {
        System.err.println("Open");
        try {
            socket.send(System.getenv("USER") + ":has joined!");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
        
    public void onMessage(WebSocket socket, Frame frame) {
        if(!frame.toString().startsWith(System.getenv("USER"))){
            try {
                socket.send(System.getenv("USER") + ":(echo)" + frame.toString());
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

socket.setBlockingMode(false);
socket.connect();
socket.awaitClose();
```
