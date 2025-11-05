import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Server {

    private record ClientData(
        Set<Integer> ackedSeqNumbers,
        InetAddress clientHost,
        int clientPort,
        String uniqueId
    ) {
        @Override
        public String toString() {
            return this.uniqueId;
        }
    }

    private static Runnable makeSenderRoutine(ClientData clientData) {
        Runnable routine = () -> {
            System.out.println("[INFO] Sender spawned for " + clientData.uniqueId());
            while (true) {
                // TODO: Go through the ackedSeqNumbers and send anything which they have not
                // yet ACKed. If all are ACKed, send the FIN packet and then exit.

                // TODO: Pause for 5 seconds (non-blocking because it's a virtual thread) and wait
                // while the receiver thread marks off more ACKs in the set
            }
        };
        return routine;
    }

    private static Runnable makeReceiverRoutine() throws SocketException {
        // Init this socket outside the routine because it can throw exceptions
        DatagramSocket socket = new DatagramSocket(Registry.SERVER_PORT);
        Runnable routine = () -> {
            // This map doesn't need to be concurrent, we only have one listener thread
            Map<String, ClientData> clientIdToData = new HashMap<>();
            // Listen for packets
            byte[] buffer = new byte[Registry.BUFFER_SIZE];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (Exception e) {
                    System.err.println("Failed while receiving data " + e.getMessage());
                    return;
                }
                Packet tcpPacket = Packet.packetFromString(
                    new String(packet.getData(), 0, packet.getLength())
                );
                String clientUniqueId = packet.getAddress() + ":" + packet.getPort();
                switch (tcpPacket.type()) {
                    case PacketType.INIT -> {
                        // Populate the clientIdToData map with this new client
                        ClientData clientData = new ClientData(
                            // This needs to be concurrent because it will be written to by this thread
                            // upon receiving ACKs, and read from by the thread dedicated to the client
                            ConcurrentHashMap.newKeySet(),
                            packet.getAddress(),
                            packet.getPort(),
                            clientUniqueId
                        );
                        clientIdToData.put(clientUniqueId, clientData);
                        System.out.println("[INFO] New client registered with ID " + clientUniqueId);
                        // Spawn a virtual thread to handle sends to this client
                        Thread.startVirtualThread(makeSenderRoutine(clientData));
                    }
                    case PacketType.ACK -> {
                        // TODO: Add this sequence number to the client's set
                        // DON'T need to do anything else, like send a FIN packet. The thread
                        // dedicated to the client will handle this.
                    }
                    case PacketType.DATA, PacketType.FIN -> {
                        System.err.println("[ERROR] Received " + tcpPacket.type().name() +
                            " packet from client, aborting.");
                        return;
                    }
                }
            }
        };
        return routine;
    }

    public static void main(String[] args) {
        Runnable receiverRoutine;
        try {
            receiverRoutine = Server.makeReceiverRoutine();
        } catch (Exception e) {
            System.err.println("Failed to setup socket receiver " + e.getMessage());
            return;
        }
        var receiver = Thread.startVirtualThread(receiverRoutine);
        try {
            receiver.join();
        } catch (Exception e) {
            System.err.println("Failed while joining listener " + e.getMessage());
        }
    }
}
