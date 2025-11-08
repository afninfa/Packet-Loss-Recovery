import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;


import org.json.JSONObject;

public class Common {
    static String phrase() {
        return "The Transmission Control Protocol (TCP) is one of the main protocols of the Internet"
            + " protocol suite. It originated in the initial network implementation in which it"
            + " complemented the Internet Protocol (IP). Therefore, the entire suite is commonly"
            + " referred to as TCP/IP. TCP provides reliable, ordered, and error-checked delivery of a"
            + " stream of octets (bytes) between applications running on hosts communicating via an IP"
            + " network. Major internet applications such as the World Wide Web, email, remote"
            + " administration, and file transfer rely on TCP, which is part of the transport layer of"
            + " the TCP/IP suite. SSL/TLS often run on top of TCP.";
    }

    static void sendPacket(Packet logicalPacket, InetAddress server, int port, DatagramSocket socket) {
        byte[] logicalPacketSerial = logicalPacket.toString().getBytes();
        DatagramPacket physicalPacket = new DatagramPacket(
            logicalPacketSerial,
            logicalPacketSerial.length,
            server,
            port
        );
        try {
            socket.send(physicalPacket);
        } catch (Exception e) {
            System.err.println("Failed to send packet "
                + logicalPacket.toString()
                + " error "
                + e.getMessage()
            );
        }
    }

    public static String packTelemetryInfo(OpenTelemetry openTelemetry) {
        Map<String, String> carrier = new HashMap<>();
        TextMapSetter<Map<String, String>> setter = (map, key, value) -> {
            map.put(key, value);
        };
        
        openTelemetry.getPropagators()
            .getTextMapPropagator()
            .inject(Context.current(), carrier, setter);
        
        var packed = new JSONObject(carrier).toString();

        return packed;
    }

    public static void unpackTelemetryInfo(String packed) {
        // TODO
    }
}
