import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private static final int BUFFER_SIZE = 2048;
    private static final int SERVER_PORT = 8080;
    private static final String LOCALHOST = "localhost";

    public static Runnable makeReceiverRoutine(
        DatagramSocket socket,
        Map<Integer, String> messagePieces,
        InetAddress server
    ) {
        return () -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            // Exits when we receive a "FIN" packet
            mainLoop: while (true) {
                // Wait for next packet
                try {
                    socket.receive(packet);
                } catch (Exception e) {
                    System.err.println("Failed to receive data: " + e.getMessage());
                    socket.close();
                    break mainLoop;
                }
                // Check for truncation
                if (packet.getLength() == buffer.length) {
                    System.out.println("[WARN] Entire buffer was used, packet may be truncated!");
                }
                // Parse the packet string e.g. "DATA|3|hello world!"
                Packet tcpPacket = Packet.packetFromString(new String(packet.getData()));
                switch (tcpPacket.type()) {
                    case FIN -> {
                        // If it's a FIN packet, exit. If the FIN packet is lost, this will be a
                        // bug, but protecting against FIN packet loss is out of scope for this
                        // project.
                        System.out.println("[INFO] Received FIN packet");
                        break mainLoop;
                    }
                    case DATA -> {
                        // Add to hashmap
                        messagePieces.put(tcpPacket.sequenceNumber(), tcpPacket.messageChunk());
                        // Send ACK
                        System.out.println("[INFO] Sending ACK for " + tcpPacket.sequenceNumber());
                        try {
                            Client.sendAckPacket(
                                tcpPacket.sequenceNumber(),
                                server,
                                socket
                            );
                        } catch (Exception e) {
                            System.err.println("Could not send ACK packet " + e);
                        }
                    }
                    case ACK, INIT -> {
                        System.err.println(
                            "Server should only send FIN or DATA, sent " + tcpPacket.type().name());
                        break mainLoop;
                    }
                }
            }
        };
    }

    private static void sendAckPacket(
        Integer sequenceNumber,
        InetAddress server,
        DatagramSocket socket
    ) throws IOException {
        Packet ackPacket = new Packet(PacketType.ACK, sequenceNumber, "");
        byte[] ackPacketSerial = ackPacket.toString().getBytes();
        DatagramPacket ackPacketSend = new DatagramPacket(
            ackPacketSerial,
            ackPacketSerial.length,
            server,
            SERVER_PORT
        );
        socket.send(ackPacketSend);
    }

    public static void main(String[] args) {
        DatagramSocket socket;
        Map<Integer, String> messagePieces = new HashMap<>();
        // Get server address
        InetAddress server;
        try {
            server = InetAddress.getByName(LOCALHOST);
        } catch (Exception e) {
            System.err.println("Could not resolve localhost " + e);
            return;
        }
        // Make socket
        try {
            socket = new DatagramSocket(SERVER_PORT);
        } catch (Exception e) {
            System.err.println("Failed to create socket: " + e.getMessage());
            return;
        }
        // Set up receiver first
        Thread receiver = Thread.startVirtualThread(
            makeReceiverRoutine(socket, messagePieces, server)
        );
        // Send an INIT packet to the server
        Packet initPacket = new Packet(PacketType.INIT, -1, "");
        byte[] initPacketSerial = initPacket.toString().getBytes();
        DatagramPacket initPacketSend = new DatagramPacket(
            initPacketSerial,
            initPacketSerial.length,
            server,
            8080
        );
        try {
            socket.send(initPacketSend);
        } catch (Exception e) {
            System.err.println("Failed to send INIT packet " + e.getMessage());
        }
        // When the receiver is done, reconstruct the message
        try {
            receiver.join();
        } catch (Exception e) {
            System.err.println("Main thread failed on the .join() " + e.getMessage());
            return;
        }
        // messagePieces.keySet().stream().sorted().forEach(key -> {
        //     System.out.print(messagePieces.get(key));
        // });
        String fullMessage = messagePieces.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .reduce("", (s1, s2) -> s1 + " | " + s2); // Visualise packet boundaries
        System.out.println(fullMessage);
    }
}
