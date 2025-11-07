import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Client {

    public static Runnable makeReceiverRoutine(
        DatagramSocket socket,
        Map<Integer, String> messagePieces,
        InetAddress server
    ) {
        Random rand = new Random();
        Runnable routine = () -> {
            byte[] buffer = new byte[Registry.BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            // Exits when we receive a "FIN" packet
            mainLoop: while (true) {
                // Wait for next packet
                try {
                    socket.receive(packet);
                } catch (Exception e) {
                    System.err.println("Failed to receive data: " + e.getMessage());
                    break mainLoop;
                }
                // Check for truncation
                if (packet.getLength() == buffer.length) {
                    System.out.println("WARNING: Entire buffer was used, packet may be truncated!");
                }
                // Parse the packet string e.g. "DATA|3|hello world!"
                Packet tcpPacket = Packet.packetFromString(
                    new String(packet.getData(), 0, packet.getLength())
                );
                switch (tcpPacket.type()) {
                    case FIN -> {
                        // If it's a FIN packet, exit. If the FIN packet is lost, this will be a
                        // bug, but protecting against FIN packet loss is out of scope for this
                        // project.
                        System.out.println("Received FIN packet");
                        break mainLoop;
                    }
                    case DATA -> {
                        // Simulate packet loss
                        if (rand.nextInt(10) < 5) {
                            // Add to hashmap
                            messagePieces.put(tcpPacket.sequenceNumber(), tcpPacket.messageChunk());
                            // Send ACK
                            System.out.println("Sending ACK for " + tcpPacket.sequenceNumber());
                            try {
                                Packet ackPacket = new Packet(
                                    PacketType.ACK,
                                    tcpPacket.sequenceNumber(),
                                    Registry.PAYLOAD_EMPTY
                                );
                                Common.sendPacket(ackPacket, server, Registry.SERVER_PORT, socket);
                            } catch (Exception e) {
                                System.err.println("Could not send ACK packet " + e);
                            }
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
        return routine;
    }

    public static void run() {
        DatagramSocket socket;
        Map<Integer, String> messagePieces = new HashMap<>();
        // Get server address
        InetAddress server;
        try {
            server = InetAddress.getByName(Registry.LOCALHOST);
        } catch (Exception e) {
            System.err.println("Could not resolve localhost " + e);
            return;
        }
        // Make socket
        try {
            // Don't specify the port, let the OS choose. This way, we can test with multiple
            // concurrent clients.
            socket = new DatagramSocket();
        } catch (Exception e) {
            System.err.println("Failed to create socket: " + e.getMessage());
            return;
        }
        // Set up receiver first
        Thread receiver = Thread.startVirtualThread(
            Client.makeReceiverRoutine(socket, messagePieces, server)
        );
        // Send an INIT packet to the server
        Packet initPacket = new Packet(PacketType.INIT, -1, Registry.PAYLOAD_EMPTY);
        Common.sendPacket(initPacket, server, Registry.SERVER_PORT, socket);
        // When the receiver is done, reconstruct the message
        try {
            receiver.join();
        } catch (Exception e) {
            System.err.println("Main thread failed on the .join() " + e.getMessage());
            return;
        }
        String fullMessage = messagePieces.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .reduce("Re-ordered message:", (s1, s2) -> s1 + "\n> " + s2);
        System.out.println(fullMessage);
    }
}
