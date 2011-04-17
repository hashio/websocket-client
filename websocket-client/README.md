WebSocket Client
Copyright 2011 Takahiro Hashimoto

Support WebSocket Draft76, Draft06

Usage
=====



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
    Packet dump to show to console
    
    - ALL     ... All sending,receiving are dump
    - HS_UP   ... Handshake sending dump
    - HS_DOWN ... Handshake receiving dump
    - FR_UP   ... Frame sending dump
    - FR_DOWN ... Frame receiving dump


####Example
    System.setProperty("websocket.packatdump", String.valueOf(
                                             PacketDumpStreamHandler.ALL
                                     ));

####Example: No dump a FR_DOWN    
    System.setProperty("websocket.packatdump", String.valueOf(
    		                                   PacketDumpStreamHandler.HS_UP
    		                                 | PacketDumpStreamHandler.HS_DOWN
    		                                 | PacketDumpStreamHandler.FR_UP
    	                              ));
