WebSocket Client
Copyright 2011 Takahiro Hashimoto

Support WebSocket Draft76, Draft06

Usage
=====

<script src="https://gist.github.com/949562.js"> </script>

        
#### Example: Sample of Jetty7 websocket chat servlet

<script src="https://gist.github.com/949572.js"> </script>


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
