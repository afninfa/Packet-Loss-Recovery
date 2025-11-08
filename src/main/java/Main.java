import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        if (args.length != 1) {
            Main.usage();
            System.err.println("ERROR: got: " + Arrays.stream(args).reduce("|", (x,y)->x+y)+"|");
            return;
        }

        // Instantiating the component will recursively instantiate all the
        // dependencies (open telemetry objects, server and client)
        TelemetryComponent component = DaggerTelemetryComponent.builder()
            .telemetryModule(new TelemetryModule(args[0]))
            .build();

        switch (args[0]) {
            case "server" -> {
                component.getServer().run();
            }
            case "client" -> {
                component.getClient().run();
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
