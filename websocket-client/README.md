WebSocket Client
Copyright 2011 Takahiro Hashimoto

Support WebSocket Draft76, Draft06

Usage
=====



System Property
===============

- websocket.origin (Default:none)  
    use Origin header

- websocket.bufferSize (Default:32767)  
    received buffer size  
    If you ever receive large frames to increase the buffer size

- websocket.queueSize (Default:500)  
    send queue size  
    If you exceeded the size of the queue, an exception will be thrown

- websocket.packatdump  
    For debug  
    Packet Dump to show to console
    