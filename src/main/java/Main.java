import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        if (args.length != 1) {
            Main.usage();
            System.err.println("ERROR: got: " + Arrays.stream(args).reduce("|", (x,y)->x+y)+"|");
            return;
        }

        TelemetryComponent component = DaggerTelemetryComponent.builder()
            .telemetryModule(new TelemetryModule(args[0]))
            .build();

        switch (args[0]) {
            case "server" -> {
                Server server = component.getServer();
                server.run();
            }
            case "client" -> {
                Client client = component.getClient();
                client.run();
            }
            default -> {
                Main.usage();
                System.err.println("ERROR: got " + args[0]);
                return;
            }
        }
    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("gradle run --args=\"client\"");
        System.err.println("gradle run --args=\"server\"");
    }
}
