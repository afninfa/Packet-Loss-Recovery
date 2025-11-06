
# Packet Loss Recovery Algorithm

Brushing up on my knowledge of TCP by implementing sequence numbers and ACK responses over UDP.

Begin by splitting data into chunks. Each chunk gets an index called a sequence number.

```
The Transm ission Con trol Proto col (TCP)  is one of  
|--------| |--------| |--------| |--------| |--------| 
    00         01         02         03         04    

the main p rotocols o f the Inte rnet proto col suite.
|--------| |--------| |--------| |--------| |--------|
    05         06         07         08         09
```

The message is sent using the following general structure

![architecture](architecture.jpg)

The server has **one receiver thread** which does the following
- spawns a new sender thread (see below) and a thread-safe `Set<Integer>` for each new connecting client
- when an existing client sends an ACK for a particular sequence number, add the number to the thread-safe `Set<Integer>` for this client

Each **sender thread** does the following
- send all the chunks which the client has not yet ACKed
- sleep for 5 seconds and repeat

Note that because we use **virtual threads,** the sleeping thread won't schedule busy waiting
instructions like it would in an OS thread.

Here is an example of the server logs with two concurrent clients.

```
Packet-Loss-Recovery $ make run-server
javac -d build Server.java
java -cp build Server
|RECEIVER| Sender spawned for /127.0.0.1:43701
|SENDER /127.0.0.1:43701| Sent sequence numbers [0, 1, 2, 3, 4, 5, 6]
|RECEIVER| Client /127.0.0.1:43701 ACKs 0
|RECEIVER| Client /127.0.0.1:43701 ACKs 1
|RECEIVER| Client /127.0.0.1:43701 ACKs 4
|RECEIVER| Client /127.0.0.1:43701 ACKs 6
|RECEIVER| Sender spawned for /127.0.0.1:37965
|SENDER /127.0.0.1:37965| Sent sequence numbers [0, 1, 2, 3, 4, 5, 6]
|RECEIVER| Client /127.0.0.1:37965 ACKs 2
|RECEIVER| Client /127.0.0.1:37965 ACKs 3
|RECEIVER| Client /127.0.0.1:37965 ACKs 5
|SENDER /127.0.0.1:43701| Sent sequence numbers [2, 3, 5]
|RECEIVER| Client /127.0.0.1:43701 ACKs 2
|RECEIVER| Client /127.0.0.1:43701 ACKs 3
|RECEIVER| Client /127.0.0.1:43701 ACKs 5
|SENDER /127.0.0.1:37965| Sent sequence numbers [0, 1, 4, 6]
|RECEIVER| Client /127.0.0.1:37965 ACKs 0
|RECEIVER| Client /127.0.0.1:37965 ACKs 1
|RECEIVER| Client /127.0.0.1:37965 ACKs 6
|SENDER /127.0.0.1:43701| Sent FIN packet
|SENDER /127.0.0.1:37965| Sent sequence numbers [4]
|SENDER /127.0.0.1:37965| Sent sequence numbers [4]
|SENDER /127.0.0.1:37965| Sent sequence numbers [4]
|RECEIVER| Client /127.0.0.1:37965 ACKs 4
|SENDER /127.0.0.1:37965| Sent FIN packet
```
