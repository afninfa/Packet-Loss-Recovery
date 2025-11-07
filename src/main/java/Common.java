import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

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

    // public static Tracer setupOpenTelemetry(String serviceName) {
    //     Resource resource = Resource.getDefault()
    //         .toBuilder()
    //         .put("service.name", serviceName)
    //         .build();

    //     // Choose transport: gRPC
    //     OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
    //         .setEndpoint("http://localhost:4317")  // default OTLP/gRPC port
    //         .build();

    //     SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
    //         .setResource(resource)
    //         .addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build())
    //         .build();

    //     OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
    //         .setTracerProvider(tracerProvider)
    //         .build();

    //     Tracer tracer = openTelemetry.getTracer("example-service");
        
    //     return tracer;
    // }
}
