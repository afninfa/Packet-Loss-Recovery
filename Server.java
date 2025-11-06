import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;


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

        public void acknowledge(Integer sequenceNumber) {
            this.ackedSeqNumbers.add(sequenceNumber);
        }
    }

    private static Runnable makeSenderRoutine(
        ClientData clientData,
        List<String> messagePieces
    ) throws SocketException {
        DatagramSocket socket = new DatagramSocket(); // Not a receiver, random port
        Runnable routine = () -> {
            int numberOfPackets = messagePieces.size() - 1;
            System.out.println("[INFO] Sender spawned for " + clientData.uniqueId());
            while (true) {
                // Look through every sequence number
                List<Integer> seqNumbersSent = IntStream.range(0, numberOfPackets)
                    .boxed()
                    .filter(currSeqNumber -> {
                        // If it's been ACKed, return false
                        if (clientData.ackedSeqNumbers().contains(currSeqNumber)) {
                            return false;
                        }
                        // If no ACK, send the packet and return true
                        Packet dataPacket = new Packet(
                            PacketType.DATA,
                            currSeqNumber,
                            messagePieces.get(currSeqNumber)
                        );
                        Common.sendPacket(
                            dataPacket,
                            clientData.clientHost,
                            clientData.clientPort,
                            socket
                        );
                        return true;
                    })
                    .toList();
                System.out.println("Sent sequence numbers " + seqNumbersSent);

                // If no packets were sent, send the FIN packet and then exit
                if (seqNumbersSent.size() == 0) {
                    Packet finPacket = new Packet(PacketType.FIN, -1, "");
                    Common.sendPacket(
                        finPacket,
                        clientData.clientHost,
                        clientData.clientPort,
                        socket
                    );
                    return;
                }

                // Pause for 5 seconds (non-blocking because it's a virtual thread)
                // During this time, if the receiver thread gets ACKs, it will mark them
                // in clientData.ackedSeqNumbers
                try {
                    Thread.sleep(Duration.ofSeconds(5));
                } catch (Exception e) {
                    System.err.println("Sender for client "
                        + clientData.uniqueId
                        + " failed while sleeping "
                        + e.getMessage()
                    );
                }
            }
        };
        return routine;
    }

    private static Runnable makeReceiverRoutine(List<String> messagePieces) throws SocketException {
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
                        try {
                            Thread.startVirtualThread(makeSenderRoutine(clientData, messagePieces));
                        } catch (Exception e) {
                            System.err.println("Failed to start thread for client "
                                + clientUniqueId
                                + " error "
                                + e.getMessage()
                            );
                        }
                    }
                    case PacketType.ACK -> {
                        if (!clientIdToData.containsKey(clientUniqueId)) {
                            System.err.println("Received an ACK from an unknown client " + clientUniqueId);
                            return;
                        }
                        clientIdToData.get(clientUniqueId).acknowledge(tcpPacket.sequenceNumber());
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
        List<String> messagePieces = new ArrayList<>();
        for (int i = 0; i < Common.phrase().length(); i += Registry.TCP_PAYLOAD_SIZE) {
            int end = Math.min(i + Registry.TCP_PAYLOAD_SIZE, Common.phrase().length());
            messagePieces.add(Common.phrase().substring(i, end));
        }
        Runnable receiverRoutine;
        try {
            receiverRoutine = Server.makeReceiverRoutine(messagePieces);
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
