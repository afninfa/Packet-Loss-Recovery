public class Registry {
    public static final int BUFFER_SIZE = 2048;
    public static final int SERVER_PORT = 8080;
    public static final int TCP_PAYLOAD_SIZE = 100;
    public static final String LOCALHOST = "localhost";
    public static final String PAYLOAD_EMPTY = "-"; // Genuinely empty strings break serialisation
    public static final String JAEGER_ENDPOINT = "http://localhost:4317";
}
