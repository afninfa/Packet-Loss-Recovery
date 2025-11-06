import java.util.Base64;

public record Packet(
    PacketType type, 
    Integer sequenceNumber,
    String messageChunk
) {
    @Override
    public String toString() {
        byte[] messageChunkBytes = this.messageChunk().getBytes();
        return String.join("|",
            // Name and sequence number won't contain "|", don't need to encode
            this.type.name(),
            String.valueOf(this.sequenceNumber()),
            // Message might contain "|", must be encoded
            Base64.getEncoder().encodeToString(messageChunkBytes)
        );
    }

    public static Packet packetFromString(String packetSerial) {
        // e.g. "DATA|3|hello world!"
        String[] parts = packetSerial.split("\\|");
        var result = new Packet(
            PacketType.valueOf(parts[0]),
            Integer.valueOf(parts[1]),
            new String(Base64.getDecoder().decode(parts[2]))
        );
        return result;
    }
}
