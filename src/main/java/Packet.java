import java.util.Base64;
import java.util.Optional;

public record Packet(
    PacketType type, 
    Integer sequenceNumber,
    String messageChunk,
    Optional<String> telemetryContext
) {
    // Alternative constructor without telemetry context
    public Packet(PacketType type, Integer sequenceNumber, String messageChunk) {
        this(type, sequenceNumber, messageChunk, Optional.empty());
    }

    @Override
    public String toString() {
        byte[] messageChunkBytes = this.messageChunk().getBytes();
        return String.join("|",
            // Name and sequence number won't contain "|", don't need to encode
            this.type.name(),
            String.valueOf(this.sequenceNumber()),
            // Message might contain "|", must be encoded
            Base64.getEncoder().encodeToString(messageChunkBytes),
            // Unsure if this may contain "|", out of scope for the project
            this.telemetryContext().orElse(Registry.NO_CONTEXT)
        );
    }

    public static Packet packetFromString(String packetSerial) {
        // e.g. "DATA|3|hello world!"
        String[] parts = packetSerial.split("\\|");
        var result = new Packet(
            PacketType.valueOf(parts[0]),
            Integer.valueOf(parts[1]),
            new String(Base64.getDecoder().decode(parts[2])),
            switch (parts[3]) {
                case Registry.NO_CONTEXT -> Optional.empty();
                default -> Optional.of(parts[3]);
            }
        );
        return result;
    }
}
